package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.ArtistsRankedDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;

import java.io.IOException;
import java.util.*;

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

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistsRankedDto>> attrHandlers = List.of(
        new HistoryAttributeHandler<>(LastfmAttribute.MBID, true, false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, ArtistsRankedDto::getMbid),
        new HistoryAttributeHandler<>(LastfmAttribute.URL,true, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, ArtistsRankedDto::getUrl),
        new SnapshotAttributeHandler<>(LastfmAttribute.RANK, false, true,
            (ArtistsRankedDto dto) -> dto.getRecordInfo().getRank())
    );

    @Override
    protected ApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TopArtistsDtoRoot dtoRoot = parseResponse(response);

        final String logPrefix = String.format("Lastfm %s response processing", LastfmApiCallType.TAG_TOP_TAGS.getMethod());
        log.info("{}: start processing DTO of type {} with {} records",
            logPrefix, dtoRoot.getClass().getName(), dtoRoot.getTopArtists().getArtists().size());

        List<ArtistsRankedDto> dtos = dtoRoot.getTopArtists().getArtists();
        List<LastfmArtist> existingArtists = artistRepository.findAllByNameIn(dtos.stream()
            .map(dto -> dto.getName()).toList());
        EntityMappingTemplate<LastfmArtist, ArtistsRankedDto> mappingTemplate = new EntityMappingTemplate<>(
            dtos, existingArtists,
            response,
            new LastfmArtistEntityFactory(),
            attrHandlers
        );

        //  save new and updated records
        List<LastfmArtist> savedArtists = mappingTemplate.saveUpdatedEntities(artistRepository::saveAll);
        log.info("{}: created/updated {} artists", logPrefix, savedArtists.size());

        //  save new entity relations
        mappingTemplate.saveEntityRelations(entityRelationService::upsertEntityRelations);
        log.info("{}: upserted ? tag-artist relations (number of actual inserts is unknown)", logPrefix);

        //  finalize and save attributes
        List<LastfmAttributeHistoryRecord> savedAttrValues =
            mappingTemplate.saveUpdatedAttrValues(attributeHistoryService::upsertCandidateValues);
        log.info("{}: created/updated {} entity attr values", logPrefix, savedAttrValues.size());

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, dtoRoot.getClass().getName());
    }
}
