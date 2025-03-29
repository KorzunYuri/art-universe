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
    private static class ArtistMapping {

        // holding response is required for extracting snapshotId and apiCallId later
        final LastfmApiResponse response;

        final ArtistsRankedDto dto;

        LastfmArtist entity;

        // Attributes require entity_id, which is missing in case of new entities.
        // Hence, we store attribute builders to finalize them later when we have all the entities.
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newAttrValuesBuilders = new ArrayList<>();

        // marks mappings containing new entities - they must be revisited to assign entity_id to their attributes
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

        //  create mapping and map existing entities to dtos
        Map<String, ArtistMapping> mappings = createDtoToEntityMapping(response, parsed);
        List<LastfmArtist> existingArtists = artistRepository.findAllByNameIn(mappings.keySet());
        assignExistingEntitiesToDtos(mappings, existingArtists);

        //  find out whether entity has to be saved
        prepareMappingsForSavingArtists(mappings);

        //  init new entities
        mappings.forEach((n, b) -> {
            if (b.entity == null) b.entity = createArtist(b);
        });

        //  save new and updated records
        List<LastfmArtist> artistsToSave = mappings.values().stream()
            .filter(m -> m.shouldBeSaved)
            .map(m -> m.entity)
            .toList();
        List<LastfmArtist> createdArtists = artistRepository.saveAll(artistsToSave);
        log.info("{}: saved {} artists", logPrefix, artistsToSave.size());

        //  update mapping with new ids. After that, all the mapping will have entities
        assignExistingEntitiesToDtos(mappings, createdArtists);

        //  save new entity relations
        List<LastfmEntityRelation> relations = createdArtists.stream()
            .map(a -> generateEntityRelation(response, a))
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("{}: upserted {} tag-artist relations (number of actual inserts is unknown)", logPrefix, relations.size());

        //  finalize and save attributes
        List<LastfmAttributeHistoryRecord> attrValuesToSave = mappings.values().stream()
                .flatMap(
                    m -> m.newAttrValuesBuilders.stream()
                        .map(bldr ->
                            bldr.entityId(m.entity.getId())
                                .entityType(m.entity.getType())
                                .build()))
            .toList();
        List<LastfmAttributeHistoryRecord> savedAttrValues = attributeHistoryService.upsertCandidateValues(attrValuesToSave);
        log.info("{}: saved {} attribute values", logPrefix, savedAttrValues.size());

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, parsed.getClass().getName());
    }

    private static LastfmEntityRelation generateEntityRelation(LastfmApiResponse response, LastfmArtist a) {
        return LastfmEntityRelation.builder()
            .scopeEntityType(response.getApiCall().getEntityType())
            .scopeEntityId(response.getApiCall().getEntityId())
            .entityType(a.getType())
            .entityId(a.getId())
            .apiCall(response.getApiCall())
            .build();
    }

    private void prepareMappingsForSavingArtists(Map<String, ArtistMapping> mappings) {
        mappings.forEach((n, m) -> prepareMappingForSavingArtists(m));
    }

    private void prepareMappingForSavingArtists(ArtistMapping mapping) {
        if (mapping.entity == null) {
            prepareMappingForSavingNewEntity(mapping);
        } else {
            prepareMappingForSavingExistingEntity(mapping);
        }
    }

    /**
     * <p>Updates mapping with new entity and marks it for saving.</p>
     * <p>Also, marks mapping as new, to update attribute records with entity id later, after entity will be saved</p>
     */
    private void prepareMappingForSavingNewEntity(ArtistMapping mapping) {
        ArtistsRankedDto dto = mapping.dto;
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newValues = mapping.newAttrValuesBuilders;

        newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.URL)
            .stringValue(dto.getUrl()));
        newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.MBID)
            .stringValue(dto.getMbid()));
        newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.RANK)
            .intValue(dto.getRecordInfo().getRank())
            .scopeEntityType(mapping.response.getApiCall().getEntityType())
            .scopeEntityId(mapping.response.getApiCall().getEntityId()));
        mapping.entity = createArtist(mapping);
        mapping.isNew = true;
        mapping.shouldBeSaved = true;
    }

    /**
     * Update existing values in entity and mark for saving
     */
    private void prepareMappingForSavingExistingEntity(ArtistMapping mapping) {
        LastfmArtist artist = mapping.entity;
        ArtistsRankedDto dto = mapping.dto;
        List<LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder> newValues = mapping.newAttrValuesBuilders;
        if (!Objects.equals(artist.getUrl(), dto.getUrl())) {
            newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.URL)
                .stringValue(dto.getUrl()));
            artist.setUrl(dto.getUrl());
            mapping.shouldBeSaved = true;
        }
        if (!Objects.equals(artist.getMbid(), dto.getMbid())) {
            newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.MBID)
                .stringValue(dto.getMbid()));
            artist.setMbid(dto.getMbid());
            mapping.shouldBeSaved = true;
        }
        newValues.add(initAttrValueBuilder(mapping, LastfmAttribute.RANK)
            .intValue(dto.getRecordInfo().getRank())
            .scopeEntityType(mapping.response.getApiCall().getEntityType())
            .scopeEntityId(mapping.response.getApiCall().getEntityId()));
    }

    private Map<String, ArtistMapping> createDtoToEntityMapping(LastfmApiResponse response, TopArtistsDtoRoot parsed) {
        return parsed.getTopArtists().getArtists().stream()
            .collect(Collectors.toMap(
                ArtistsRankedDto::getName,
                dto -> new ArtistMapping(response, dto)));
    }

    private void assignExistingEntitiesToDtos(Map<String, ArtistMapping> mappings, List<LastfmArtist> entities) {
        entities.forEach(artist -> mappings.get(artist.getName()).entity = artist);
    }

    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder initAttrValueBuilder(
        ArtistMapping mapping, LastfmAttribute attribute) {
        LastfmApiCall apiCall = mapping.response.getApiCall();
        return LastfmAttributeHistoryRecord.builder()
            .attribute(attribute)
            .apiCallId(apiCall.getId());
    }

    private LastfmArtist createArtist(ArtistMapping mapping) {
        ArtistsRankedDto dto = mapping.dto;
        return LastfmArtist.builder()
            .name(dto.getName())
            .url(dto.getUrl())
            .mbid(dto.getMbid())
            .apiCall(mapping.response.getApiCall())
            .build();
    }
}
