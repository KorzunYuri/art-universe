package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.ArtistsRankedDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopArtistsResponseProcessor extends LastfmApiResponseProcessor<TopArtistsDtoRoot> {

    private final LastfmArtistRepository artistRepository;
    private final LastfmEntityRelationService entityRelationService;
    private final LastfmAttributeHistoryService attributeHistoryService;

    protected LastfmTagTopArtistsResponseProcessor(
        LastfmArtistRepository artistRepository,
        LastfmEntityRelationService entityRelationService,
        LastfmAttributeHistoryService attributeHistoryService
    ) {
        super(TopArtistsDtoRoot.class);

        this.artistRepository = artistRepository;
        this.entityRelationService = entityRelationService;
        this.attributeHistoryService = attributeHistoryService;
    }

    @Override
    protected ApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @RequiredArgsConstructor
    private static class ArtistBinding {

        // holding response is required for extracting snapshotId and apiCallId later
        final LastfmApiResponse response;

        final ArtistsRankedDto dto;

        LastfmArtist entity;

        // Attributes require entity_id, which is missing in case of new entities.
        // Hence, we store attribute builders to finalize them later when we have the entities.
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newAttrValuesBuilders = new ArrayList<>();

        // marks bindings containing new entities - they must be revisited to assign entity_id to their attributes
        boolean isNew = false;

        // marks non-changed entities to eliminate unnecessary saving
        boolean shouldBeSaved = false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TopArtistsDtoRoot parsed = parseResponse(response);

        final String logPrefix = String.format("Lastfm %s response processing", LastfmApiCallType.TAG_TOP_TAGS.getMethod());
        log.info("{}: start processing DTO of type {} with {} records",
                logPrefix, parsed.getClass().getName(), parsed.getTopArtists().getArtists().size());

        Map<String, ArtistBinding> dtoBindingByName = parsed.getTopArtists().getArtists().stream()
                .collect(Collectors.toMap(ArtistsRankedDto::getName, dto -> new ArtistBinding(response, dto)));

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
        List<LastfmArtist> savedNewArtists = artistRepository.saveAll(artistsToSave);
        log.info("{}: saved {} artists", logPrefix, artistsToSave.size());

        //  save new entity relations
        List<LastfmEntityRelation> relations = savedNewArtists.stream()
                .map(a -> LastfmEntityRelation.builder()
                    .scopeEntityType(response.getApiCall().getEntityType())
                    .scopeEntityId(response.getApiCall().getEntityId())
                    .entityType(a.getType())
                    .entityId(a.getId())
                    .apiCall(response.getApiCall())
                .build())
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("{}: saved tag-artist relations", logPrefix);

        //  save new attribute values with artist ids we have just acquired
        final List<LastfmAttributeHistoryRecord> newAttrValues = new ArrayList<>();
        savedNewArtists.forEach(a -> {
            ArtistBinding binding = dtoBindingByName.get(a.getName());
            binding.newAttrValuesBuilders.forEach(v -> {
                if (binding.isNew) {
                    v.entityId(a.getId());
                }
                // finalize builder and make a new attribute value to be saved
                newAttrValues.add(v.build());
            });
        });
        List<LastfmAttributeHistoryRecord> savedAttrValues = attributeHistoryService.upsertCandidateValues(newAttrValues);
        log.info("{}: saved {} attribute values", logPrefix, savedAttrValues.size());

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
        LastfmArtist artist = binding.entity;
        ArtistsRankedDto dto = binding.dto;
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newValues = binding.newAttrValuesBuilders;
        if (!Objects.equals(artist.getUrl(), dto.getUrl())) {
            newValues.add(
                    createAttrValueBuilderForEntity(binding, LastfmAttribute.URL)
                            .stringValue(dto.getUrl()));
            artist.setUrl(dto.getUrl());
            binding.shouldBeSaved = true;
        }
        if (!Objects.equals(artist.getMbid(), dto.getMbid())) {
            newValues.add(
                    createAttrValueBuilderForEntity(binding, LastfmAttribute.MBID)
                            .stringValue(dto.getMbid()));
            artist.setMbid(dto.getMbid());
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

        newValues.add(
                createAttrValueBuilder(binding, LastfmAttribute.URL)
                    .stringValue(dto.getUrl()));
        newValues.add(createAttrValueBuilder(binding, LastfmAttribute.MBID)
                .stringValue(dto.getMbid()));
        binding.entity = createArtist(binding);
        binding.isNew = true;
        binding.shouldBeSaved = true;
    }

    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder createAttrValueBuilderForEntity(
            ArtistBinding binding, LastfmAttribute attribute) {
        return createAttrValueBuilder(binding, attribute)
                .entityType((LastfmEntityType) binding.entity.getType())
                .entityId(binding.entity.getId());
    }

    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder createAttrValueBuilder(
            ArtistBinding binding, LastfmAttribute attribute) {
        LastfmApiCall apiCall = binding.response.getApiCall();
        return LastfmAttributeHistoryRecord.builder()
                .attribute(attribute)
                .entityType(LastfmEntityType.ARTIST)
                .apiCallId(apiCall.getId())
                .scopeEntityType(apiCall.getEntityType())
                .scopeEntityId(apiCall.getEntityId());
    }

    private LastfmArtist createArtist(ArtistBinding binding) {
        ArtistsRankedDto dto = binding.dto;
        return LastfmArtist.builder()
                .name(dto.getName())
                .url(dto.getUrl())
                .mbid(dto.getMbid())
                .apiCall(binding.response.getApiCall())
            .build();
    }
}
