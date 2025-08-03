package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmAlbumTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmAlbumGetInfoResponseProcessor extends LastfmApiResponseProcessor<AlbumGetInfoDtoRoot> {

    // Services
    private final LastfmAlbumService albumService;
    private final LastfmArtistService artistService;
    private final LastfmTrackService trackService;
    private final LastfmArtistTrackService artistTrackService;
    private final LastfmAlbumTrackService albumTrackService;
    private final LastfmAlbumTagService albumTagService;
    private final LastfmTagService tagService;
    private final EntityFactory<LastfmArtist, AlbumGetInfoTrackArtistDto> artistFactory;
    private final EntityFactory<LastfmAlbum, AlbumGetInfoAlbumDto> albumFactory;
    private final EntityFactory<LastfmTag, AlbumGetInfoTagDto> tagFactory;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    // Attribute handlers for album
    private static final List<EntityAttributeHandler<LastfmAlbum, ?, AlbumGetInfoAlbumDto>> albumAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmAlbum, AlbumGetInfoAlbumDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmAlbum.class, AlbumGetInfoAlbumDto.class);
        albumAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount")
        );
    }

    // Attribute handlers for track
    private static final List<EntityAttributeHandler<LastfmTrack, ?, AlbumGetInfoTrackDto>> trackAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTrack, AlbumGetInfoTrackDto> factory = 
            new EntityAttributeHandlerFactory<>(LastfmTrack.class, AlbumGetInfoTrackDto.class);
        trackAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.DURATION, false, "duration")
        );
    }

    // Attribute handlers for tracks' artists
    private static final List<EntityAttributeHandler<LastfmArtist, ?, AlbumGetInfoTrackArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, AlbumGetInfoTrackArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, AlbumGetInfoTrackArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url")
        );
    }

    // Attribute handlers for tag
    private static final List<EntityAttributeHandler<LastfmTag, ?, AlbumGetInfoTagDto>> tagAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTag, AlbumGetInfoTagDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmTag.class, AlbumGetInfoTagDto.class);
        tagAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.URL, false, "url")
        );
    }

    public LastfmAlbumGetInfoResponseProcessor(
        LastfmAlbumService albumService,
        LastfmArtistService artistService,
        LastfmTrackService trackService,
        LastfmArtistTrackService artistTrackService,
        LastfmAlbumTrackService albumTrackService,
        LastfmAlbumTagService albumTagService,
        LastfmTagService tagService,
        EntityFactory<LastfmArtist, AlbumGetInfoTrackArtistDto> artistFactory,
        EntityFactory<LastfmAlbum, AlbumGetInfoAlbumDto> albumFactory,
        EntityFactory<LastfmTag, AlbumGetInfoTagDto> tagFactory,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(AlbumGetInfoDtoRoot.class);
        this.albumService = albumService;
        this.artistService = artistService;
        this.trackService = trackService;
        this.artistTrackService = artistTrackService;
        this.albumTrackService = albumTrackService;
        this.albumTagService = albumTagService;
        this.tagService = tagService;
        this.artistFactory = artistFactory;
        this.albumFactory = albumFactory;
        this.tagFactory = tagFactory;
        this.dtoProcessingService = dtoProcessingService;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ALBUM_GET_INFO;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        AlbumGetInfoDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        validateRootDto(dtoRoot, sourceApiResponse);

        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();

        LastfmAlbum album = albumService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new IllegalStateException("Album not found for ID: " + sourceApiCall.getEntityId()));

        AlbumGetInfoAlbumDto albumDto = dtoRoot.getAlbum();

        // Step 1: Process artists from tracks (if available)
        Map<String, LastfmArtist> artistsByUrl = Collections.emptyMap();
        if (hasTracks(albumDto)) {
            var artistsResult = processArtists(albumDto, sourceApiCall);
            artistsByUrl = artistsResult.actualEntities().stream()
                .collect(Collectors.toMap(LastfmArtist::getUrl, Function.identity(), (a1, a2) -> a1));
        } else {
            log.info("Album {} has no tracks, skipping artist processing", albumDto.getName());
        }
        
        // Step 2: Process album
        var albumResult = processAlbum(albumDto, sourceApiCall);
        LastfmAlbum updatedAlbum = albumResult.actualEntities().get(0);
        
        // Step 3: Process tracks (if available)
        if (hasTracks(albumDto)) {
            var trackResult = processTracks(albumDto.getTracksObject().getTracks(), artistsByUrl, sourceApiCall);
            
            // Step 4: Create artist-track relationships
            createArtistTrackRelationships(trackResult.actualEntities(), sourceApiCall);

            // Step 5: Create album-track relationships with positions
            createAlbumTrackRelationships(updatedAlbum, trackResult, sourceApiCall);
        }
        
        // Step 6: Process tags if available
        if (hasTags(albumDto)) {
            processAlbumTags(updatedAlbum, albumDto.getTags().getTag(), sourceApiCall);
        } else {
            log.info("Album {} has no tags, skipping tag processing", albumDto.getName());
        }
    }

    private static boolean hasTracks(AlbumGetInfoAlbumDto albumDto) {
        return albumDto.getTracksObject() != null
            && albumDto.getTracksObject().getTracks() != null
            && !albumDto.getTracksObject().getTracks().isEmpty();
    }
    
    private static boolean hasTags(AlbumGetInfoAlbumDto albumDto) {
        return albumDto.getTags() != null 
            && albumDto.getTags().getTag() != null 
            && !albumDto.getTags().getTag().isEmpty();
    }

    private void validateRootDto(AlbumGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        if (dtoRoot.getAlbum() == null) {
            throw new IllegalArgumentException(String.format("No album data in response %s", sourceApiResponse.getId()));
        }
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, AlbumGetInfoTrackArtistDto> processArtists(
        AlbumGetInfoAlbumDto albumDto, LastfmApiCall sourceApiCall
    ) {
        List<AlbumGetInfoTrackArtistDto> artistDtos = albumDto.getTracksObject().getTracks().stream()
            .map(AlbumGetInfoTrackDto::getArtist)
            .filter(artist -> artist != null) // Filter out null artists
            .distinct()
            .toList();

        if (artistDtos.isEmpty()) {
            log.info("No valid artists found in tracks for album {}", albumDto.getName());
            return createEmptyProcessingResult(sourceApiCall);
        }

        LastfmApiDtoProcessingResult<LastfmArtist, AlbumGetInfoTrackArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            artistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );

        return result;
    }

    private static <E extends BaseLastfmEntity, D extends EntityDto<E>> LastfmApiDtoProcessingResult<E, D> createEmptyProcessingResult(LastfmApiCall sourceApiCall) {
        return new LastfmApiDtoProcessingResult<>(List.of(), List.of(), new EntityMappingResult<>(Map.of(), sourceApiCall));
    }

    private LastfmApiDtoProcessingResult<LastfmAlbum, AlbumGetInfoAlbumDto> processAlbum(
        AlbumGetInfoAlbumDto albumDto, 
        LastfmApiCall sourceApiCall
    ) {
        // Process album - we're updating an existing album
        List<AlbumGetInfoAlbumDto> albumDtos = List.of(albumDto);
        LastfmApiDtoProcessingResult<LastfmAlbum, AlbumGetInfoAlbumDto> result = dtoProcessingService.process(
            sourceApiCall,
            albumDtos,
            albumFactory,
            albumAttrHandlers,
            albumService
        );
        
        log.info("Updated album: {}", result.actualEntities().get(0).getName());
        return result;
    }
    
    private LastfmApiDtoProcessingResult<LastfmTrack, AlbumGetInfoTrackDto> processTracks(
        List<AlbumGetInfoTrackDto> trackDtos,
        Map<String, LastfmArtist> artistsByUrl,
        LastfmApiCall sourceApiCall
    ) {
        if (trackDtos == null || trackDtos.isEmpty()) {
            log.info("No tracks to process");
            return createEmptyProcessingResult(sourceApiCall);
        }
        
        // Create track factory with artist map
        EntityFactory<LastfmTrack, AlbumGetInfoTrackDto> trackFactory = 
            new LastfmAlbumGetInfoTrackFactory(artistsByUrl);
        
        // Process tracks
        LastfmApiDtoProcessingResult<LastfmTrack, AlbumGetInfoTrackDto> result = dtoProcessingService.process(
            sourceApiCall,
            trackDtos,
            trackFactory,
            trackAttrHandlers,
            trackService
        );
        
        log.info("Processed {} tracks", result.actualEntities().size());
        return result;
    }
    
    private void createArtistTrackRelationships(
        List<LastfmTrack> tracks,
        LastfmApiCall sourceApiCall
    ) {
        if (tracks.isEmpty()) {
            log.info("No tracks to create artist-track relationships");
            return;
        }
        
        List<LastfmArtistTrack> relationships = new ArrayList<>();
        
        for (LastfmTrack track : tracks) {
            if (track.getArtist() != null) {
                LastfmArtistTrack relationship = LastfmArtistTrack.builder()
                    .artist(track.getArtist())
                    .track(track)
                    .apiCall(sourceApiCall)
                    .build();
                relationships.add(relationship);
            }
        }
        
        if (!relationships.isEmpty()) {
            artistTrackService.upsertAll(relationships);
            log.info("Created {} artist-track relationships", relationships.size());
        } else {
            log.info("No artist-track relationships to create (no tracks with artists)");
        }
    }
    
    private void createAlbumTrackRelationships(
        LastfmAlbum album,
        LastfmApiDtoProcessingResult<LastfmTrack, AlbumGetInfoTrackDto> trackResult,
        LastfmApiCall sourceApiCall
    ) {
        if (trackResult.entityMapping().getMap().isEmpty()) {
            log.info("No track mappings to create album-track relationships");
            return;
        }
        
        List<LastfmAlbumTrack> relationships = new ArrayList<>();
        
        trackResult.entityMapping().forEach((url, mapping) -> {
            LastfmTrack track = mapping.getNewEntity();
            AlbumGetInfoTrackDto dto = mapping.getDto();
            
            LastfmAlbumTrack relationship = LastfmAlbumTrack.builder()
                .album(album)
                .track(track)
                .position(dto.getAttr() != null ? dto.getAttr().getRank() : null)
                .apiCall(sourceApiCall)
                .build();
            
            relationships.add(relationship);
        });
        
        if (!relationships.isEmpty()) {
            albumTrackService.upsertAll(relationships);
            log.info("Created {} album-track relationships for album {}", relationships.size(), album.getName());
        }
    }
    
    private void processAlbumTags(
        LastfmAlbum album,
        List<AlbumGetInfoTagDto> tagDtos,
        LastfmApiCall sourceApiCall
    ) {
        if (tagDtos == null || tagDtos.isEmpty()) {
            log.info("No tags to process for album {}", album.getName());
            return;
        }
        
        // Process tags
        LastfmApiDtoProcessingResult<LastfmTag, AlbumGetInfoTagDto> result = dtoProcessingService.process(
            sourceApiCall,
            tagDtos,
            tagFactory,
            tagAttrHandlers,
            tagService
        );
        
        if (result.actualEntities().isEmpty()) {
            log.info("No tags were processed for album {}", album.getName());
            return;
        }
        
        // Create album-tag relationships
        List<LastfmAlbumTag> relationships = new ArrayList<>();
        result.actualEntities().forEach(tag -> {
            LastfmAlbumTag relationship = LastfmAlbumTag.builder()
                .album(album)
                .tag(tag)
                .apiCall(sourceApiCall)
                .build();
                
            relationships.add(relationship);
        });
        
        if (!relationships.isEmpty()) {
            albumTagService.upsertAll(relationships);
            log.info("Created {} album-tag relationships for album {}", relationships.size(), album.getName());
        }
    }
}
