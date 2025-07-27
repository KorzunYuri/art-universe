package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopTracksResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTracksDtoRoot> {

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmTrackService trackService;
    private final LastfmArtistService artistService;
    private final LastfmArtistTrackService artistTrackService;
    private final EntityFactory<LastfmArtist, ArtistTopTracksTrackArtistDto> artistFactory;

    @Value("${lastfm.client.methods.artist.topTracks.trackListenersThreshold:1000}")
    private int trackListenersThreshold;

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
        LastfmApiDtoProcessingService dtoProcessingService,
        LastfmArtistTopTracksArtistFactory artistFactory
    ) {
        super(ArtistTopTracksDtoRoot.class);

        this.trackService = trackService;
        this.artistService = artistService;
        this.artistTrackService = artistTrackService;
        this.dtoProcessingService = dtoProcessingService;
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

        // First, extract and save artists from track metadata
        // We shouldn't use the artist we generated api call for:
        // some artists share mbid (which is used as api call parameter),
        // but the 'true' artist will always be in track's metadata
        var artistMappingResult = updateArtists(dtoRoot, sourceApiCall);

        // Then use the saved artists when creating tracks
        var trackMappingResult = updateTracks(dtoRoot, sourceApiCall, artistMappingResult);

        // Finally, create relationships between tracks and their artists
        bindTracksToArtists(trackMappingResult, artistMappingResult, sourceApiCall);
    }

    /**
     * Extracts artists from track metadata and saves them
     */
    private LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> updateArtists(
        ArtistTopTracksDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall
    ) {
        // Extract artist DTOs from tracks
        List<ArtistTopTracksTrackArtistDto> artistDtos = dtoRoot.getRootObject().getTracks().stream()
            .filter(dto -> dto.getListenersCount() >= trackListenersThreshold)
            .map(ArtistTopTracksTrackDto::getArtist)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        // Process and save artists
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            artistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("saved {} track artists", result.savedEntities().size());
        log.info("saved {} track artists' attributes", result.savedAttributeValues().size());

        return result;
    }

    /**
     * Creates and saves tracks using artist information from the previous step
     */
    private LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> updateTracks(
        ArtistTopTracksDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall,
        LastfmApiDtoProcessingResult<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMappingResult
    ) {
        List<ArtistTopTracksTrackDto> trackDtos = filterTracksForSaving(dtoRoot.getRootObject().getTracks());

        // Create track factory with artist mapping results
        LastfmArtistTopTracksTrackFactory trackFactory = new LastfmArtistTopTracksTrackFactory(artistMappingResult);

        // Process DTOs and save tracks
        LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> result = dtoProcessingService.process(
            sourceApiCall,
            trackDtos,
            trackFactory,
            trackAttrHandlers,
            trackService
        );
        log.info("saved {} tracks", result.savedEntities().size());
        log.info("saved {} tracks' attributes", result.savedAttributeValues().size());

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
                String artistName = trackMapping.getDto().getArtist().getName();
                
                // Find corresponding artist mapping
                EntityMapping<LastfmArtist, ArtistTopTracksTrackArtistDto> artistMapping = 
                    artistMappingResult.entityMapping().get(artistName);
                
                if (artistMapping == null) {
                    log.warn("Artist not found for track {}", trackMapping.getDto().getName());
                    return null;
                }
                
                LastfmArtist artist = artistMapping.getNewEntity();
                if (artist == null) {
                    log.warn("Artist {} wasn't saved", artistName);
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

    /**
     * Filters tracks based on listeners count threshold
     */
    private List<ArtistTopTracksTrackDto> filterTracksForSaving(List<ArtistTopTracksTrackDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getListenersCount() >= trackListenersThreshold)
            .toList();
    }
}
