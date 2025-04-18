package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.processing;

import lombok.extern.slf4j.Slf4j;
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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopTracksResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTracksDtoRoot> {

    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmTrackService trackService;
    private final EntityFactory<LastfmTrack, ArtistTopTracksTrackDto> trackEntityFactory;

    protected LastfmArtistTopTracksResponseProcessor(
        LastfmApiDtoProcessingService dtoProcessingService,
        LastfmTrackService trackService
    ) {
        super(ArtistTopTracksDtoRoot.class);
        
        this.dtoProcessingService = dtoProcessingService;
        this.trackService = trackService;

        this.trackEntityFactory = new LastfmArtistTopTracksTrackEntityFactory();
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, ArtistTopTracksTrackDto>> trackAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmTrack::getUrl, LastfmTrack::setUrl, ArtistTopTracksTrackDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID, false,
            LastfmTrack::getMbid, LastfmTrack::setMbid, ArtistTopTracksTrackDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.LISTENERS_COUNT, false,
            LastfmTrack::getListenersCount, LastfmTrack::setListenersCount, ArtistTopTracksTrackDto::getListenersCount),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.PLAY_COUNT, false,
            LastfmTrack::getPlayCount, LastfmTrack::setPlayCount, ArtistTopTracksTrackDto::getPlayCount),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE, false,
            LastfmTrack::getStreamable, LastfmTrack::setStreamable,
            (dto) -> 1 == dto.getStreamable())
    );

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

        List<ArtistTopTracksTrackDto> trackDtos = dtoRoot.getRootObject().getTracks();
        List<String> trackUrls = trackDtos.stream().map(ArtistTopTracksTrackDto::getUrl).toList();
        List<LastfmTrack> existingTracks = trackService.findAllByUrls(trackUrls);

        LastfmApiDtoProcessingResult<LastfmTrack> result = dtoProcessingService.processDtosWithRelations(
            trackDtos, existingTracks, sourceApiResponse,
            trackEntityFactory,
            trackAttrHandlers,
            trackService::saveTracks
        );
        log.info("saved {} artist's tracks", result.savedEntities().size());
        log.info("saved {} artist's tracks' attributes", result.savedAttributeValues().size());
        log.info("saved {} artist-track relations", result.savedEntityRelations().size());
    }
}
