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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopTracksResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTracksDtoRoot> {

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmTrackService trackService;
    private final LastfmArtistService artistService;
    private final EntityFactory<LastfmTrack, ArtistTopTracksTrackDto> trackEntityFactory;

    @Value("${lastfm.client.methods.artist.topTracks.trackListenersThreshold:1000}")
    private int trackListenersThreshold;
    private final LastfmArtistTrackService artistTrackService;

    protected LastfmArtistTopTracksResponseProcessor(
        LastfmTrackService trackService,
        LastfmArtistService artistService,
        LastfmArtistTrackService artistTrackService,
        EntityFactory<LastfmTrack, ArtistTopTracksTrackDto> trackEntityFactory,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(ArtistTopTracksDtoRoot.class);

        this.trackService = trackService;
        this.artistService = artistService;
        this.artistTrackService = artistTrackService;
        this.trackEntityFactory = trackEntityFactory;
        this.dtoProcessingService = dtoProcessingService;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, ArtistTopTracksTrackDto>> trackAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTrack, ArtistTopTracksTrackDto> factory = new EntityAttributeHandlerFactory<>(LastfmTrack.class, ArtistTopTracksTrackDto.class);
        trackAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount")
        );
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmArtist artist = artistService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source artist with ID=%s not found", sourceApiCall.getEntityId())));

        var tracksMappingResult = updateTracks(dtoRoot, sourceApiCall);

        bindTracksToArtist(tracksMappingResult, artist, sourceApiCall);
    }

    private LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> updateTracks(
        ArtistTopTracksDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall
    ) {
        List<ArtistTopTracksTrackDto> trackDtos = filterTracksForSaving(dtoRoot.getRootObject().getTracks());

        // Process DTOs and save tracks
        LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> result = dtoProcessingService.process(
            sourceApiCall,
            trackDtos,
            trackEntityFactory,
            trackAttrHandlers,
            trackService
        );
        log.info("saved {} artist's tracks", result.savedEntities().size());
        log.info("saved {} artist's tracks' attributes", result.savedAttributeValues().size());

        return result;
    }

    private void bindTracksToArtist(
        LastfmApiDtoProcessingResult<LastfmTrack, ArtistTopTracksTrackDto> tracksMappingResult,
        LastfmArtist artist,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTrack> relations = tracksMappingResult.savedEntities().stream()
            .map(track -> LastfmArtistTrack.builder()
                    .apiCall(sourceApiCall)
                    .artist(artist)
                    .track(track)
                .build()
            )
            .collect(Collectors.toList());
        artistTrackService.upsertAll(relations);
        log.info("saved {} artist-track relations", relations.size());
    }

    private List<ArtistTopTracksTrackDto> filterTracksForSaving(List<ArtistTopTracksTrackDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getListenersCount() >= trackListenersThreshold)
            .toList();
    }
}
