package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo.*;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.factory.album.getinfo.LastfmAlbumGetInfoTrackFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.ArtistDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.TrackDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmAlbumTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmAlbumTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistTrackService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
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
        LastfmApiDtoProcessingOrchestrator dtoProcessingService,
        DtoQualityService dtoQualityService,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(AlbumGetInfoDtoRoot.class, objectMapper);
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
            Map<String, LastfmArtist> artistsByName = artistsResult.actualEntities().stream()
                .collect(Collectors.toMap(LastfmArtist::getName, Function.identity(), (a1, a2) -> a1));

            // If no quality artists were found, skip track processing
            if (!artistsByName.isEmpty()) {
                // Step 3: Process tracks (if available)
                var trackResult = processTracks(albumDto.getTracksObject().getTracks(), artistsByName, sourceApiCall);

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
            processAlbumTags(updatedAlbum, albumDto.getTags().getTags(), sourceApiCall);
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
            && albumDto.getTags().getTags() != null
            && !albumDto.getTags().getTags().isEmpty();
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
        artistDtos = ArtistDeduplicationUtils.deduplicateArtistDtos(artistDtos);

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
        var result = dtoProcessingService.process(
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
        Map<String, LastfmArtist> artistsByName,
        LastfmApiCall sourceApiCall
    ) {
        if (trackDtos == null || trackDtos.isEmpty()) {
            log.info("No tracks to process");
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        var dedupedDtos = TrackDeduplicationUtils.deduplicateTrackDtos(trackDtos);
        var qualityTracks = dtoQualityService.validateAgainstBlacklist(dedupedDtos).stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .filter(track -> track.getArtist() != null)
            .filter(track -> artistsByName.containsKey(track.getArtist().getName()))
            .toList();

        if (qualityTracks.isEmpty()) {
            log.info("No quality tracks found after validation");
            return LastfmApiDtoProcessingResult.empty(sourceApiCall);
        }

        // Create track factory with artist map
        EntityFactory<LastfmTrack, AlbumGetInfoTrackDto> trackFactory =
            new LastfmAlbumGetInfoTrackFactory(artistsByName);

        // Process tracks
        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityTracks,
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
        var result = dtoProcessingService.process(
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
