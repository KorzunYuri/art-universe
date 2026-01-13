package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.toptracks.LastfmArtistTopTracksArtistFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.artist.toptracks.LastfmArtistTopTracksTrackFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.ArtistDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.TrackDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistTrackService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopTracksResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTracksDtoRoot> {

    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
    private final LastfmTrackService trackService;
    private final LastfmArtistService artistService;
    private final LastfmArtistTrackService artistTrackService;
    private final DtoQualityService dtoQualityService;
    private final EntityFactory<LastfmArtist, ArtistTopTracksTrackArtistDto> artistFactory;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistTopTracksTrackArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistTopTracksTrackArtistDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistTopTracksTrackArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url")
        );
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, ArtistTopTracksTrackDto>> trackAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTrack, ArtistTopTracksTrackDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmTrack.class, ArtistTopTracksTrackDto.class);
        trackAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount")
        );
    }

    protected LastfmArtistTopTracksResponseProcessor(
        LastfmTrackService trackService,
        LastfmArtistService artistService,
        LastfmArtistTrackService artistTrackService,
        LastfmApiDtoProcessingOrchestrator dtoProcessingService,
        DtoQualityService dtoQualityService,
        LastfmArtistTopTracksArtistFactory artistFactory,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(ArtistTopTracksDtoRoot.class, objectMapper);

        this.trackService = trackService;
        this.artistService = artistService;
        this.artistTrackService = artistTrackService;
        this.dtoProcessingService = dtoProcessingService;
        this.dtoQualityService = dtoQualityService;
        this.artistFactory = artistFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();

        // First, extract and validate artists from track metadata
        // We get ALL artists and then validate them against blacklist
        var artistMappingResult = updateArtists(dtoRoot, sourceApiCall);

        // Then validate and process tracks using DtoQualityService
        var trackMappingResult = updateTracks(dtoRoot, sourceApiCall, artistMappingResult);

        // Finally, create relationships between tracks and their artists
        bindTracksToArtists(trackMappingResult, artistMappingResult, sourceApiCall);
    }

    /**
     * Extracts all artists from track metadata, validates them against blacklist, and saves them
     */
    private LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> updateArtists(
        ArtistTopTracksDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall
    ) {
        // Extract ALL artist DTOs from tracks (no filtering by track metrics)
        List<ArtistTopTracksTrackArtistDto> allArtistDtos = dtoRoot.getRootObject().getTracks().stream()
            .map(ArtistTopTracksTrackDto::getArtist)
            .filter(Objects::nonNull)
            .toList();
        allArtistDtos = ArtistDeduplicationUtils.deduplicateArtistDtos(allArtistDtos);

        // Validate artists against blacklist
        var qualityArtistDtos = dtoQualityService.validateAgainstBlacklist(allArtistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityArtistDtos.size() < allArtistDtos.size()) {
            log.info("Filtered out {} blacklisted artists from tracks", 
                allArtistDtos.size() - qualityArtistDtos.size());
        }

        // Process and save artists
        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityArtistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("saved {} track artists", result.actualEntities().size());
        log.info("saved {} track artists' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    /**
     * Validates tracks using DtoQualityService and creates them using artist information from the previous step
     */
    private LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> updateTracks(
        ArtistTopTracksDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall,
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMappingResult
    ) {
        List<ArtistTopTracksTrackDto> allTrackDtos = TrackDeduplicationUtils.deduplicateTrackDtos(dtoRoot.getRootObject().getTracks()).stream()
            .filter(track -> track.getArtist() != null
                && artistMappingResult.entityMapping().getMap().containsKey(track.getArtist()))
            .toList();

        // Validate tracks using DtoQualityService (includes threshold and blacklist validation)
        var qualityTrackDtos = dtoQualityService.validateAndBlacklist(allTrackDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        // Create track factory with artist mapping results
        LastfmArtistTopTracksTrackFactory trackFactory = new LastfmArtistTopTracksTrackFactory(artistMappingResult);

        // Process DTOs and save tracks
        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityTrackDtos,
            trackFactory,
            trackAttrHandlers,
            trackService
        );
        log.info("saved {} tracks", result.actualEntities().size());
        log.info("saved {} tracks' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    /**
     * Creates relationships between tracks and their artists
     */
    private void bindTracksToArtists(
        LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> trackMappingResult,
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMappingResult,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTrack> relations = trackMappingResult.entityMapping().values().stream()
            .map(trackMapping -> {
                // Get artist name from track DTO
                var artistDto = trackMapping.getDto().getArtist();
                
                // Find corresponding artist mapping
                EntityMapping<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMapping = 
                    artistMappingResult.entityMapping().get(artistDto);
                
                if (artistMapping == null) {
                    log.warn("Artist not found for track {} - {} ", artistDto.getName(), trackMapping.getDto().getName());
                    return null;
                }
                
                LastfmArtist artist = artistMapping.getNewEntity();
                if (artist == null) {
                    log.warn("Artist {} wasn't saved", artistDto);
                    return null;
                }
                
                return LastfmArtistTrack.builder()
                    .apiCall(sourceApiCall)
                    .artist(artist)
                    .track(trackMapping.getNewEntity())
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        
        artistTrackService.upsertAll(relations);
        log.info("saved {} artist-track relations", relations.size());
    }
}
