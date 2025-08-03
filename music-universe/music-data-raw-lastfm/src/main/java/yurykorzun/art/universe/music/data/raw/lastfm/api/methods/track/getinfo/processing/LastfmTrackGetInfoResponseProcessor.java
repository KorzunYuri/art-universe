package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmTrackTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmTrackTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTrackGetInfoResponseProcessor extends LastfmApiResponseProcessor<TrackGetInfoDtoRoot> {

    // entity services
    private final LastfmTrackService trackService;
    private final LastfmArtistService artistService;
    private final LastfmAlbumService albumService;
    private final LastfmTagService tagService;
    // relation services
    private final LastfmArtistTrackService artistTrackService;
    private final LastfmTrackTagService trackTagService;
    private final LastfmAlbumTrackService albumTrackService;
    // factories
    private final EntityFactory<LastfmArtist, TrackGetInfoArtistDto> artistFactory;
    private final EntityFactory<LastfmAlbum, TrackGetInfoAlbumDto> albumFactory;
    private final EntityFactory<LastfmTag, TrackGetInfoTagDto> tagFactory;
    // common services
    private final LastfmApiDtoProcessingService dtoProcessingService;

    protected LastfmTrackGetInfoResponseProcessor(
        // entity services
        LastfmTrackService trackService,
        LastfmArtistService artistService,
        LastfmAlbumService albumService,
        LastfmTagService tagService,
        // relation services
        LastfmArtistTrackService artistTrackService,
        LastfmAlbumTrackService albumTrackService,
        LastfmTrackTagService trackTagService,
        // factories
        EntityFactory<LastfmArtist, TrackGetInfoArtistDto> artistFactory,
        EntityFactory<LastfmAlbum, TrackGetInfoAlbumDto> albumFactory,
        EntityFactory<LastfmTag, TrackGetInfoTagDto> tagFactory,
        // common services
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(TrackGetInfoDtoRoot.class);

        this.trackService = trackService;
        this.artistService = artistService;
        this.albumService = albumService;
        this.tagService = tagService;

        this.artistTrackService = artistTrackService;
        this.albumTrackService = albumTrackService;
        this.trackTagService = trackTagService;

        this.artistFactory = artistFactory;
        this.albumFactory = albumFactory;
        this.tagFactory = tagFactory;

        this.dtoProcessingService = dtoProcessingService;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, TrackGetInfoTrackDto>> trackAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTrack, TrackGetInfoTrackDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmTrack.class, TrackGetInfoTrackDto.class);
        trackAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.DURATION, false, "duration"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount", "listeners"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount", "playcount")
        );
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TrackGetInfoArtistDto>> artistAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmArtist.class, TrackGetInfoArtistDto.class)
            .createHandler(LastfmAttribute.MBID, false, "mbid"),
        new EntityAttributeHandlerFactory<>(LastfmArtist.class, TrackGetInfoArtistDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    private static final List<EntityAttributeHandler<LastfmAlbum, ?, TrackGetInfoAlbumDto>> albumAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmAlbum.class, TrackGetInfoAlbumDto.class)
            .createHandler(LastfmAttribute.MBID, false, "mbid"),
        new EntityAttributeHandlerFactory<>(LastfmAlbum.class, TrackGetInfoAlbumDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    private static final List<EntityAttributeHandler<LastfmTag, ?, TrackGetInfoTagDto>> tagAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmTag.class, TrackGetInfoTagDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TRACK_GET_INFO;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        
        TrackGetInfoDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();

        // Step 1. Process artist
        var artistResult = processArtist(dtoRoot, sourceApiCall);
        LastfmArtist artist = artistResult.actualEntities().get(0);

        // Step 2. Process track with artist reference
        var trackResult = processTrack(dtoRoot, sourceApiCall, artist);
        LastfmTrack track = trackResult.actualEntities().get(0);
        
        // Step 3. Create artist-track relationship
        createArtistTrackRelationship(artist, track, sourceApiCall);

        // Step 4. Process album if present
        if (dtoRoot.getTrack().getAlbum() != null) {
            LastfmApiDtoProcessingResult<LastfmAlbum, TrackGetInfoAlbumDto> albumResult = processAlbum(dtoRoot, sourceApiCall);
            if (!albumResult.actualEntities().isEmpty()) {
                LastfmAlbum album = albumResult.actualEntities().get(0);
                
                // Create album-track relationship
                createAlbumTrackRelationship(album, track, dtoRoot.getTrack().getAlbum().getPosition(), sourceApiCall);
            }
        }

        // Step 5. Process tags
        LastfmApiDtoProcessingResult<LastfmTag, TrackGetInfoTagDto> tagResult = processTags(dtoRoot, sourceApiCall);
        
        // Step 6. Create track-tag relationships
        createTrackTagRelationships(track, tagResult.actualEntities(), sourceApiCall);
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, TrackGetInfoArtistDto> processArtist(
        TrackGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall
    ) {
        TrackGetInfoArtistDto artistDto = dtoRoot.getTrack().getArtist();
        
        LastfmApiDtoProcessingResult<LastfmArtist, TrackGetInfoArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            List.of(artistDto),
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        
        log.info("Processed artist: {}", artistDto.getName());
        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmTrack, TrackGetInfoTrackDto> processTrack(
        TrackGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall, LastfmArtist artist
    ) {
        TrackGetInfoTrackDto trackDto = dtoRoot.getTrack();

        EntityFactory<LastfmTrack, TrackGetInfoTrackDto> trackFactoryWithArtist = new LastfmTrackGetInfoTrackFactory(artist);
        
        LastfmApiDtoProcessingResult<LastfmTrack, TrackGetInfoTrackDto> result = dtoProcessingService.process(
            sourceApiCall,
            List.of(trackDto),
            trackFactoryWithArtist,
            trackAttrHandlers,
            trackService
        );
        
        log.info("Processed track: {}", trackDto.getName());
        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmAlbum, TrackGetInfoAlbumDto> processAlbum(
        TrackGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall
    ) {
        TrackGetInfoAlbumDto albumDto = dtoRoot.getTrack().getAlbum();
        
        LastfmApiDtoProcessingResult<LastfmAlbum, TrackGetInfoAlbumDto> result = dtoProcessingService.process(
            sourceApiCall,
            List.of(albumDto),
            albumFactory,
            albumAttrHandlers,
            albumService
        );
        
        log.info("Processed album: {}", albumDto.getName());
        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmTag, TrackGetInfoTagDto> processTags(
        TrackGetInfoDtoRoot dtoRoot, LastfmApiCall sourceApiCall
    ) {
        List<TrackGetInfoTagDto> tagDtos = dtoRoot.getTrack().getTopTags().getTags();
        
        LastfmApiDtoProcessingResult<LastfmTag, TrackGetInfoTagDto> result = dtoProcessingService.process(
            sourceApiCall,
            tagDtos,
            tagFactory,
            tagAttrHandlers,
            tagService
        );
        
        log.info("Processed {} tags", tagDtos.size());
        return result;
    }
    
    private void createArtistTrackRelationship(LastfmArtist artist, LastfmTrack track, LastfmApiCall sourceApiCall) {
        LastfmArtistTrack artistTrack = LastfmArtistTrack.builder()
            .artist(artist)
            .track(track)
            .apiCall(sourceApiCall)
            .build();
        
        artistTrackService.upsertAll(List.of(artistTrack));
        log.info("Created artist-track relationship between artist {} and track {}", artist.getName(), track.getName());
    }
    
    private void createAlbumTrackRelationship(LastfmAlbum album, LastfmTrack track, Integer position, LastfmApiCall sourceApiCall) {
        LastfmAlbumTrack albumTrack = LastfmAlbumTrack.builder()
            .album(album)
            .track(track)
            .apiCall(sourceApiCall)
            .position(position)
            .build();
        
        albumTrackService.upsertAll(List.of(albumTrack));
        log.info("Created album-track relationship between album {} and track {}", album.getName(), track.getName());
    }
    
    private void createTrackTagRelationships(LastfmTrack track, List<LastfmTag> tags, LastfmApiCall sourceApiCall) {
        List<LastfmTrackTag> trackTags = tags.stream()
            .map(tag -> LastfmTrackTag.builder()
                .track(track)
                .tag(tag)
                .apiCall(sourceApiCall)
                .build())
            .collect(Collectors.toList());
        
        if (!trackTags.isEmpty()) {
            trackTagService.upsertAll(trackTags);
            log.info("Created {} track-tag relationships for track {}", trackTags.size(), track.getName());
        }
    }
}
