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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
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
import java.util.*;
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
    private final DtoQualityService dtoQualityService;

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
        LastfmApiDtoProcessingService dtoProcessingService,
        DtoQualityService dtoQualityService
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
        this.dtoQualityService = dtoQualityService;
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

        // Step 1: Process album
        // Root album DTO doesn't have a reference to artist so we don't need to save artist first
        AlbumGetInfoAlbumDto albumDto = dtoRoot.getAlbum();
        var albumValidationResult = dtoQualityService.validateAndBlacklist(albumDto);
        if (albumValidationResult.isRejected()) {
            log.info("Album {} didn't pass the validation: {}", album.getUrl(), albumValidationResult);
            return;
        }
        var albumResult = processAlbum(albumDto, sourceApiCall);
        LastfmAlbum updatedAlbum = albumResult.actualEntities().get(0);

        // Step 2: Process artists from tracks (if available)
        if (hasTracks(albumDto)) {
            var artistsResult = processArtists(albumDto, sourceApiCall);
            Map<String, LastfmArtist> artistsByUrl = artistsResult.actualEntities().stream()
                .collect(Collectors.toMap(LastfmArtist::getUrl, Function.identity(), (a1, a2) -> a1));

            // If no quality artists were found, skip track processing
            if (!artistsByUrl.isEmpty()) {
                // Step 3: Process tracks (if available)
                var trackResult = processTracks(albumDto.getTracksObject().getTracks(), artistsByUrl, sourceApiCall);

                // Step 4: Create artist-track relationships
                createArtistTrackRelationships(trackResult.actualEntities(), sourceApiCall);

                // Step 5: Create album-track relationships with positions
                createAlbumTrackRelationships(updatedAlbum, trackResult, sourceApiCall);
            } else {
                log.info("No quality artists found for album {}, skipping track processing", albumDto.getName());
            }
        } else {
            log.info("Album {} has no tracks, skipping artists & tracks processing", albumDto.getName());
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
        var artistDtos = albumDto.getTracksObject().getTracks().stream()
            .map(AlbumGetInfoTrackDto::getArtist)
            .filter(Objects::nonNull)
            .distinct()
            .toList();

        if (artistDtos.isEmpty()) {
            log.info("No valid artists found in tracks for album {}", albumDto.getName());
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        var qualityArtistDtos = dtoQualityService.validateAgainstBlacklist(artistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();


        if (qualityArtistDtos.isEmpty()) {
            log.info("No quality artists found after filtering for album {}", albumDto.getName());
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        if (qualityArtistDtos.size() < artistDtos.size()) {
            log.info("Filtered out {} low-quality artists for album {}",
                artistDtos.size() - qualityArtistDtos.size(), albumDto.getName());
        }

        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityArtistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );

        return result;
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
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        var qualityTracks = dtoQualityService.validateAgainstBlacklist(trackDtos).stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .filter(track -> track.getArtist() != null)
            .filter(track -> artistsByUrl.containsKey(track.getArtist().getUrl()))
            .toList();

        if (qualityTracks.isEmpty()) {
            log.info("No quality tracks found after validation");
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
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
