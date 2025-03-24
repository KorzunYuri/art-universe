package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.ArtistsRankedDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopArtistsResponseProcessor extends LastfmApiResponseProcessor<TopArtistsDtoRoot> {

    private final LastfmArtistRepository artistRepository;
    private final LastfmAttributeHistoryRecordRepository attributeHistoryRecordRepository;

    protected LastfmTagTopArtistsResponseProcessor(LastfmArtistRepository artistRepository, LastfmAttributeHistoryRecordRepository attributeHistoryRecordRepository) {
        super(TopArtistsDtoRoot.class);

        this.artistRepository = artistRepository;
        this.attributeHistoryRecordRepository = attributeHistoryRecordRepository;
    }

    @Override
    protected ApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    protected void processParsedResponse(TopArtistsDtoRoot parsed) {
        final String logPrefix = String.format("Lastfm %s response processing", LastfmApiCallType.TAG_TOP_TAGS.getMethod());
        log.info("{}: start processing DTO of type {} with {} records",
                logPrefix,
                parsed.getClass().getName(),
                parsed.getTopArtists().getArtists().size());

        Map<String, ArtistBinding> dtoBindingByName = parsed.getTopArtists().getArtists().stream()
                .collect(Collectors.toMap(ArtistsRankedDto::getName, ArtistBinding::new));

        //  bind persisted entities to dtos
        artistRepository.findAllByNameIn(dtoBindingByName.keySet()).forEach(
                artist -> dtoBindingByName.get(artist.getName()).entity = artist);

        //  generate new entities, update old entities, generate new values
        dtoBindingByName.forEach((name, binding) -> {
            updateBinding(binding);
        });

        //  save new and updated records
        List<LastfmArtist> artistsToSave = dtoBindingByName.values().stream()
                .filter(binding -> binding.shouldBeSaved)
                .map(binding -> binding.entity)
            .toList();
        artistsToSave = artistRepository.saveAll(artistsToSave);
        log.info("{}: saved {} artists", logPrefix, artistsToSave.size());

        //  update new entities' attributes with entity_ids we have just acquired
        final List<LastfmAttributeHistoryRecord> newAttrValues = new ArrayList<>();
        artistsToSave.forEach(a -> {
            ArtistBinding binding = dtoBindingByName.get(a.getName());
            binding.newAttrValuesBuilders.forEach(v -> {
                if (binding.isNew) {
                    v.entityId(a.getId());
                }
                // finalize builder and make a new attribute value to be saved
                newAttrValues.add(v.build());
            });
        });
        attributeHistoryRecordRepository.saveAll(newAttrValues);
        log.info("{}: saved {} attribute values", logPrefix, newAttrValues.size());

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, parsed.getClass().getName());
    }

    private void updateBinding(ArtistBinding binding) {
        if (binding.entity == null) {
            updateWithNewEntity(binding);
        } else {
            updateWithNewValues(binding);
        }
    }

    /**
     * Update existing values in entity and mark for saving
     */
    private void updateWithNewValues(ArtistBinding binding) {
        LastfmArtist entity = binding.entity;
        ArtistsRankedDto dto = binding.dto;
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newValues = binding.newAttrValuesBuilders;
        if (!Objects.equals(entity.getUrl(), dto.getUrl())) {
            newValues.add(
                    getAttrValueBuilderForEntity(entity, LastfmAttribute.URL)
                            .stringValue(dto.getUrl()));
            entity.setUrl(dto.getUrl());
            binding.shouldBeSaved = true;
        }
        if (!Objects.equals(entity.getMbid(), dto.getMbid())) {
            newValues.add(
                    getAttrValueBuilderForEntity(entity, LastfmAttribute.MBID)
                            .stringValue(dto.getMbid()));
            entity.setMbid(dto.getMbid());
            binding.shouldBeSaved = true;
        }
    }

    /**
     * <p>Updates binding with new entity and marks it for saving.</p>
     * <p>Also, marks binding as new, to update attribute records with entity id later, after entity will be saved</p>
     */
    private void updateWithNewEntity(ArtistBinding binding) {
        ArtistsRankedDto dto = binding.dto;
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newValues = binding.newAttrValuesBuilders;

        newValues.add(getAttrValueBuilderForDto(dto, LastfmAttribute.URL)
                .stringValue(dto.getUrl()));
        newValues.add(getAttrValueBuilderForDto(dto, LastfmAttribute.MBID)
                .stringValue(dto.getMbid()));
        binding.entity = dtoToArtist(dto);
        binding.isNew = true;
        binding.shouldBeSaved = true;
    }

    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder getAttrValueBuilderForEntity(
            LastfmArtist artist, LastfmAttribute attribute) {
        return LastfmAttributeHistoryRecord.builder()
                .attribute(attribute)
                .entityTypeId(LastfmEntityType.ARTIST)
                .entityId(artist.getId());
    }

    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder getAttrValueBuilderForDto(
            ArtistsRankedDto artist, LastfmAttribute attribute) {
        return LastfmAttributeHistoryRecord.builder()
                .attribute(attribute)
                .entityTypeId(LastfmEntityType.ARTIST);
    }

    private LastfmArtist dtoToArtist(ArtistsRankedDto dto) {
        return LastfmArtist.builder()
                .name(dto.getName())
                .url(dto.getUrl())
                .mbid(dto.getMbid())
            .build();
    }

    @RequiredArgsConstructor
    private static class ArtistBinding {
        final ArtistsRankedDto dto;
        LastfmArtist entity;

        // Attributes require entity_id, which is missing in case of new entities.
        // Hence, we store attributes builders and finalize them when we can.
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newAttrValuesBuilders = new ArrayList<>();

        // marks bindings containing new entities - they must be revisited to assign entity_id to their attributes
        boolean isNew = false;

        // marks non-changed entities to eliminate unnecessary saving
        boolean shouldBeSaved = false;
    }
}
