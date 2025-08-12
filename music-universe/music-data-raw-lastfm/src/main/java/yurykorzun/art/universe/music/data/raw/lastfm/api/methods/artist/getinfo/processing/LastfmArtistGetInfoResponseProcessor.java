package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistsRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistGetInfoResponseProcessor extends LastfmApiResponseProcessor<ArtistGetInfoDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmTagService tagService;
    private final LastfmArtistsRelationService artistsRelationService;
    private final LastfmArtistTagService artistTagService;

    private final EntityFactory<LastfmArtist, ArtistGetInfoArtistDto> artistFactory;
    private final EntityFactory<LastfmArtist, ArtistGetInfoSimilarArtistDto> similarArtistFactory;
    private final EntityFactory<LastfmTag, ArtistGetInfoArtistTagDto> tagFactory;

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final DtoQualityService dtoQualityService;

    protected LastfmArtistGetInfoResponseProcessor(
        LastfmArtistService artistService,
        LastfmTagService tagService,
        LastfmArtistsRelationService artistsRelationService,
        LastfmArtistTagService artistTagService,
        EntityFactory<LastfmArtist, ArtistGetInfoArtistDto> artistFactory,
        EntityFactory<LastfmArtist, ArtistGetInfoSimilarArtistDto> similarArtistFactory,
        EntityFactory<LastfmTag, ArtistGetInfoArtistTagDto> tagFactory,
        LastfmApiDtoProcessingService dtoProcessingService,
        DtoQualityService dtoQualityService
    ) {
        super(ArtistGetInfoDtoRoot.class);

        this.artistService = artistService;
        this.artistsRelationService = artistsRelationService;
        this.tagService = tagService;
        this.artistTagService = artistTagService;
        this.artistFactory = artistFactory;
        this.similarArtistFactory = similarArtistFactory;
        this.tagFactory = tagFactory;
        this.dtoProcessingService = dtoProcessingService;
        this.dtoQualityService = dtoQualityService;
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetInfoArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistGetInfoArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistGetInfoArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount",
                (dto) -> dto.getStats().getListeners()),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount",
                (dto) -> dto.getStats().getPlayCount())
        );
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetInfoSimilarArtistDto>> similarArtistAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistGetInfoSimilarArtistDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistGetInfoArtistTagDto>> tagAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmTag.class, ArtistGetInfoArtistTagDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        
        ArtistGetInfoDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();

        // Validate main artist first - if it fails, skip entire processing
        ArtistGetInfoArtistDto artistDto = dtoRoot.getArtist();
        var artistValidationResult = dtoQualityService.validateAndBlacklist(artistDto);
        if (artistValidationResult.isRejected()) {
            log.info("Skipping artist.getInfo processing for artist '{}': {}", 
                artistDto.getName(), artistValidationResult.getMessage());
            return;
        }

        // Step 1. Update source artist
        LastfmArtist artist = updateArtist(dtoRoot, sourceApiCall);

        // Step 2. Create similar artists and relations between artists
        var artistsBindingResult = updateSimilarArtists(dtoRoot, sourceApiCall, artist);

        // Step 3.
        bindArtistsToArtist(artistsBindingResult, artist, sourceApiCall);

        // update artist's tags
        var tagsBindingResult = updateTags(dtoRoot, sourceApiCall);

        // bind artist's tags to artist
        bindTagsToArtist(tagsBindingResult, artist, sourceApiCall);
    }

    /**
     * Returns DTOs of similar artists for saving, excluding source artist.
     */
    private List<ArtistGetInfoSimilarArtistDto> filterDtosForSaving(ArtistGetInfoDtoRoot dtoRoot, LastfmArtist sourceArtist) {
        return dtoRoot.getArtist().getSimilarArtistsObject().getArtists().stream()
            .filter(dto -> !isSameArtist(dto, sourceArtist))
            .toList();
    }
    
    /**
     * Checks if the DTO represents the same artist as the source artist.
     * Compares by MBID if both have it, otherwise by name.
     */
    private boolean isSameArtist(ArtistGetInfoSimilarArtistDto dto, LastfmArtist sourceArtist) {
        // If both have MBID, compare by MBID
        if (dto.getMbid() != null && !dto.getMbid().trim().isEmpty() && 
            sourceArtist.getMbid() != null && !sourceArtist.getMbid().trim().isEmpty()) {
            return dto.getMbid().equals(sourceArtist.getMbid());
        }
        
        // Otherwise compare by name (case-insensitive)
        return dto.getName().equalsIgnoreCase(sourceArtist.getName());
    }

    private LastfmArtist updateArtist(ArtistGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {

        ArtistGetInfoArtistDto dto = dtoRoot.getArtist();

        LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetInfoArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            List.of(dto),
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("saved artist {}", dto.getName());
        log.info("saved {} artists' attributes", result.savedAttributeValues().size());

        if (result.actualEntities().size() != 1) {
            throw new IllegalArgumentException(String.format("Expected 1 artists to be saved, got %s", result.actualEntities().size()));
        }
        return result.actualEntities().getFirst();
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetInfoSimilarArtistDto> updateSimilarArtists(
        ArtistGetInfoDtoRoot dtoRoot, 
        LastfmApiCall sourceApiCall,
        LastfmArtist sourceArtist
    ) {
        List<ArtistGetInfoSimilarArtistDto> artistDtos = filterDtosForSaving(dtoRoot, sourceArtist);

        // Validate similar artists against blacklist only (no metrics available)
        var qualitySimilarArtistDtos = dtoQualityService.validateAgainstBlacklist(artistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualitySimilarArtistDtos.isEmpty()) {
            log.info("No quality similar artists found after validation for artist {}", sourceArtist.getName());
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        if (qualitySimilarArtistDtos.size() < artistDtos.size()) {
            log.info("Filtered out {} low-quality similar artists for artist {}",
                artistDtos.size() - qualitySimilarArtistDtos.size(), sourceArtist.getName());
        }

        LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetInfoSimilarArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            qualitySimilarArtistDtos,
            similarArtistFactory,
            similarArtistAttrHandlers,
            artistService
        );
        log.info("saved {} artist's similar artists", result.actualEntities().size());
        log.info("saved {} artist's similar artists' attributes", result.savedAttributeValues().size());

        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmTag, ArtistGetInfoArtistTagDto> updateTags(ArtistGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {

        List<ArtistGetInfoArtistTagDto> dtos = dtoRoot.getArtist().getTagsObject().getTags();

        // Validate tags against blacklist only (no metrics available)
        var qualityTagDtos = dtoQualityService.validateAgainstBlacklist(dtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityTagDtos.isEmpty()) {
            log.info("No quality tags found after validation");
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        if (qualityTagDtos.size() < dtos.size()) {
            log.info("Filtered out {} low-quality tags", dtos.size() - qualityTagDtos.size());
        }

        LastfmApiDtoProcessingResult<LastfmTag, ArtistGetInfoArtistTagDto> result = dtoProcessingService.process(
            sourceApiCall,
            qualityTagDtos,
            tagFactory,
            tagAttrHandlers,
            tagService
        );
        log.info("saved {} tags", result.actualEntities().size());
        log.info("saved {} tags' attributes", result.savedAttributeValues().size());

        return result;
    }

    private void bindArtistsToArtist(
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistGetInfoSimilarArtistDto> artistsMappingResult,
        LastfmArtist artist,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistsRelation> relations = artistsMappingResult.actualEntities().stream()
            .map((similarArtist) -> LastfmArtistsRelation.builder()
                    .apiCall(sourceApiCall)
                    .sourceArtist(similarArtist)
                    .targetArtist(artist)
                    .relationType(LastfmEntityRelationType.SIMILARITY)
                .build())
            .collect(Collectors.toList());
        artistsRelationService.upsertAll(relations);
        log.info("saved {} artist-artist relations", relations.size());
    }

    private void bindTagsToArtist(
        LastfmApiDtoProcessingResult<LastfmTag, ArtistGetInfoArtistTagDto> tagsMappingResult,
        LastfmArtist artist,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTag> relations = tagsMappingResult.actualEntities().stream()
            .map((tag) -> LastfmArtistTag.builder()
                .apiCall(sourceApiCall)
                .artist(artist)
                .tag(tag)
                .build())
            .collect(Collectors.toList());
        artistTagService.upsertAll(relations);
        log.info("saved {} tag-artist relations", relations.size());
    }

}
