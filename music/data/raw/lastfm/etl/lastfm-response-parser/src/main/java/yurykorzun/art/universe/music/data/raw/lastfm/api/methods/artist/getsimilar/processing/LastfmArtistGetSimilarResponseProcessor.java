package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.processing;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto.ArtistGetSimilarDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistsRelationService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistGetSimilarResponseProcessor extends LastfmApiResponseProcessor<ArtistGetSimilarDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmArtist, ArtistGetSimilarArtistDto> artistFactory;
    private final LastfmArtistsRelationService artistsRelationService;
    private final LastfmAttributeHistoryService attributeHistoryService;
    private final DtoQualityService dtoQualityService;

    @Value("${lastfm.tasks.response-parse.methods.artist.get-similar.artist-match-threshold}")
    private float artistMatchThreshold;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetSimilarArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistGetSimilarArtistDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistGetSimilarArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url")
        );
    }

    protected LastfmArtistGetSimilarResponseProcessor(
        LastfmArtistService artistService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmArtist, ArtistGetSimilarArtistDto> artistFactory,
        LastfmArtistsRelationService artistsRelationService,
        LastfmAttributeHistoryService attributeHistoryService,
        DtoQualityService dtoQualityService,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(ArtistGetSimilarDtoRoot.class, objectMapper);

        this.artistService = artistService;
        this.dtoProcessingService = dtoProcessingService;
        this.artistFactory = artistFactory;
        this.artistsRelationService = artistsRelationService;
        this.attributeHistoryService = attributeHistoryService;
        this.dtoQualityService = dtoQualityService;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_SIMILAR;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistGetSimilarDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmArtist artist = artistService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source artist with ID=%s not found", sourceApiCall.getEntityId())));

        var similarArtistResult = updateSimilarArtists(dtoRoot, sourceApiCall, artist);

        bindArtistsToArtist(similarArtistResult, artist, sourceApiCall);
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetSimilarArtistDto> updateSimilarArtists(
        ArtistGetSimilarDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall,
        LastfmArtist sourceArtist
    ) {
        List<ArtistGetSimilarArtistDto> dtos = filterDtosForSaving(dtoRoot, sourceArtist);

        var result = dtoProcessingService.process(
            sourceApiCall,
            dtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("Saved {} similar artists", result.actualEntities().size());
        log.info("Saved {} similar artists' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    private void bindArtistsToArtist(
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetSimilarArtistDto> similarArtistsMapping, // required to extract MATCH value from DTO
        LastfmArtist artist,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistsRelation> relations = similarArtistsMapping.entityMapping().getMap().values().stream()
            .map((artistMapping) -> LastfmArtistsRelation.builder()
                    .apiCall(sourceApiCall)
                    .sourceArtist(artistMapping.getNewEntity())
                    .targetArtist(artist)
                    .matchScore(BigDecimal.valueOf(artistMapping.getDto().getMatchCoeff()))
                    .relationType(LastfmEntityRelationType.SIMILARITY)
                .build())
            .collect(Collectors.toList());
        artistsRelationService.upsertAll(relations);
        log.info("saved {} artist-artist relations", relations.size());

        List<LastfmAttributeHistoryRecord> records = relations.stream()
            .map(relation -> LastfmAttributeHistoryRecord.builder()
                .apiCallId(sourceApiCall.getId())
                .scopeEntityType(LastfmEntityType.ARTIST)
                .scopeEntityId(relation.getSourceArtist().getId())
                .entityType(LastfmEntityType.ARTIST)
                .entityId(relation.getTargetArtist().getId())
                .attribute(LastfmAttribute.MATCH_COEFF)
                .numericValue((long) relation.getMatchScore().multiply(new BigDecimal(100)).intValue())
                .build())
            .toList();
        attributeHistoryService.upsertCandidateValues(records);
        log.info("saved {} artist-artist attribute records", records.size());
    }

    /**
     * Returns DTOs of artists for saving, filtered by match coefficient, blacklist validation, and excluding source artist.
     */
    private List<ArtistGetSimilarArtistDto> filterDtosForSaving(ArtistGetSimilarDtoRoot dtoRoot, LastfmArtist sourceArtist) {
        List<ArtistGetSimilarArtistDto> candidateDtos = dtoRoot.getRootObject().getArtists().stream()
            .filter(a -> a.getMatchCoeff() > artistMatchThreshold)
            .filter(a -> !isSameArtist(a, sourceArtist))
            .toList();

        // Validate against blacklist
        var qualityArtistDtos = dtoQualityService.validateAgainstBlacklist(candidateDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityArtistDtos.size() < candidateDtos.size()) {
            log.info("Filtered out {} blacklisted similar artists for artist {}",
                candidateDtos.size() - qualityArtistDtos.size(), sourceArtist.getName());
        }

        return qualityArtistDtos;
    }
    
    /**
     * Checks if the DTO represents the same artist as the source artist.
     * Compares by MBID if both have it, otherwise by name.
     */
    private boolean isSameArtist(ArtistGetSimilarArtistDto dto, LastfmArtist sourceArtist) {
        // If both have MBID, compare by MBID
        if (dto.getMbid() != null && !dto.getMbid().trim().isEmpty() && 
            sourceArtist.getMbid() != null && !sourceArtist.getMbid().trim().isEmpty()) {
            return dto.getMbid().equals(sourceArtist.getMbid());
        }
        
        // Otherwise compare by name (case-insensitive)
        return dto.getName().equalsIgnoreCase(sourceArtist.getName());
    }
}
