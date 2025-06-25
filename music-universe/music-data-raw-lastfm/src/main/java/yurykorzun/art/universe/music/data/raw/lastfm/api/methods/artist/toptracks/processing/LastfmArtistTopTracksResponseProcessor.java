package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto.ArtistTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopTracksResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTracksDtoRoot> {

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmTrackService trackService;
    private final LastfmArtistService artistService;
    private final EntityFactory<LastfmTrack, ArtistTopTracksTrackDto> trackEntityFactory;

    @Value("${lastfm.client.methods.artist.topTracks.trackListenersThreshold:1000}")
    private int trackListenersThreshold;

    protected LastfmArtistTopTracksResponseProcessor(
        LastfmApiDtoProcessingService dtoProcessingService,
        LastfmTrackService trackService,
        LastfmArtistService artistService,
        EntityFactory<LastfmTrack, ArtistTopTracksTrackDto> trackEntityFactory
    ) {
        super(ArtistTopTracksDtoRoot.class);
        
        this.dtoProcessingService = dtoProcessingService;
        this.trackService = trackService;
        this.artistService = artistService;
        this.trackEntityFactory = trackEntityFactory;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, ArtistTopTracksTrackDto>> trackAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTrack, ArtistTopTracksTrackDto> factory = new EntityAttributeHandlerFactory<>(LastfmTrack.class, ArtistTopTracksTrackDto.class);
        trackAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount"),
            factory.createHandler(LastfmAttribute.IS_STREAMABLE, false, "isStreamable",
                (dto) -> 1 == dto.getStreamable())
        );
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        updateTracks(sourceApiResponse, dtoRoot);
    }

    private void updateTracks(LastfmApiResponse sourceApiResponse, ArtistTopTracksDtoRoot dtoRoot) {

        List<ArtistTopTracksTrackDto> trackDtos = filterDtosForSaving(dtoRoot.getRootObject().getTracks());
        List<String> trackUrls = trackDtos.stream().map(ArtistTopTracksTrackDto::getUrl).toList();
        List<LastfmTrack> existingTracks = trackService.findAllByUrls(trackUrls);

        // Get the artist from the API call
        LastfmArtist artist = artistService.findById(sourceApiResponse.getApiCall().getEntityId())
            .orElseThrow(() -> new EntityNotFoundException("Artist not found in API call entity"));

        // Process DTOs and save tracks
        LastfmApiDtoProcessingResult<LastfmTrack> result = dtoProcessingService.processDtosWithRelations(
            trackDtos, existingTracks, sourceApiResponse,
            trackEntityFactory,
            trackAttrHandlers,
            trackService::saveTracks
        );
        
        // Set artist reference for each track
        if (artist != null) {
            List<LastfmTrack> tracksToUpdate = new ArrayList<>();
            
            // Update both existing and newly saved tracks
            for (LastfmTrack track : result.savedEntities()) {
                track.setArtist(artist);
                tracksToUpdate.add(track);
            }
            
            // Save tracks with artist references
            if (!tracksToUpdate.isEmpty()) {
                trackService.saveTracks(tracksToUpdate);
                log.info("updated {} tracks with artist references", tracksToUpdate.size());
            }
        }
        
        log.info("saved {} artist's tracks", result.savedEntities().size());
        log.info("saved {} artist's tracks' attributes", result.savedAttributeValues().size());
        log.info("saved {} artist-track relations", result.savedEntityRelations().size());
    }

    private List<ArtistTopTracksTrackDto> filterDtosForSaving(List<ArtistTopTracksTrackDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getListenersCount() >= trackListenersThreshold)
            .toList();
    }
}
