package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import jakarta.persistence.EntityNotFoundException;
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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service.LastfmArtistTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopTracksResponseProcessor extends LastfmApiResponseProcessor<TagTopTracksDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmArtistService artistService;
    private final LastfmTrackService trackService;
    private final LastfmArtistTrackService artistTrackService;
    private final EntityFactory<LastfmTrack, TagTopTracksTrackDto> trackFactory;
    private final EntityFactory<LastfmArtist, TagTopTracksTrackArtistDto> artistFactory;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    private final Set<TagTopTracksDtoRoot> rootsWithMissingAttrsLogged = new HashSet<>();

    protected LastfmTagTopTracksResponseProcessor(
        LastfmTagService tagService,
        LastfmArtistService artistService,
        LastfmTrackService trackService,
        LastfmArtistTrackService artistTrackService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmTrack, TagTopTracksTrackDto> trackFactory,
        EntityFactory<LastfmArtist, TagTopTracksTrackArtistDto> artistFactory
    ) {
        super(TagTopTracksDtoRoot.class);

        this.artistService = artistService;
        this.trackService = trackService;
        this.tagService = tagService;
        this.artistTrackService = artistTrackService;
        this.trackFactory = trackFactory;
        this.artistFactory = artistFactory;
        this.dtoProcessingService = dtoProcessingService;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, TagTopTracksTrackDto>> trackAttrHandlers;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TagTopTracksTrackArtistDto>> artistAttrHandlers;
    
    static {
        EntityAttributeHandlerFactory<LastfmTrack, TagTopTracksTrackDto> trackAttrHandlerFactory =
            new EntityAttributeHandlerFactory<>(LastfmTrack.class, TagTopTracksTrackDto.class);
        trackAttrHandlers = List.of(
            trackAttrHandlerFactory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            trackAttrHandlerFactory.createHandler(LastfmAttribute.URL, false, "url"),
            trackAttrHandlerFactory.createHandler(LastfmAttribute.DURATION, false, "duration")
        );

        EntityAttributeHandlerFactory<LastfmArtist, TagTopTracksTrackArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, TagTopTracksTrackArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"));
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmTag sourceTag = tagService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source tag with ID=%s not found", sourceApiCall.getEntityId())));

        //  first save new artists
        var artistMappingResult = updateArtists(dtoRoot, sourceApiCall);

        //  then save new tracks
        var trackMappingResult = updateTracks(dtoRoot, sourceApiCall);

        //  finally, bind all tracks to artists
        bindTracksToArtists(trackMappingResult, artistMappingResult, sourceApiCall);

        //  clean cache
        rootsWithMissingAttrsLogged.remove(dtoRoot);
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> updateArtists(TagTopTracksDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {

        List<TagTopTracksTrackArtistDto> artistDtos = getArtistDtos(dtoRoot);

        LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> result = dtoProcessingService.process(
            sourceApiCall,
            artistDtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );
        log.info("saved {} tag's tracks' artists", result.savedEntities().size());
        log.info("saved {} tag's tracks' artists' attributes", result.savedAttributeValues().size());

        return result;
    }

    private LastfmApiDtoProcessingResult<LastfmTrack, TagTopTracksTrackDto> updateTracks(TagTopTracksDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {

        List<TagTopTracksTrackDto> trackDtos = getTrackDtos(dtoRoot);

        LastfmApiDtoProcessingResult<LastfmTrack, TagTopTracksTrackDto> result = dtoProcessingService.process(
            sourceApiCall,
            trackDtos,
            trackFactory,
            trackAttrHandlers,
            trackService
        );
        log.info("saved {} tag's tracks", result.savedEntities().size());
        log.info("saved {} tag's tracks' attributes", result.savedAttributeValues().size());

        return result;
    }


    private void bindTracksToArtists(
        LastfmApiDtoProcessingResult<LastfmTrack, TagTopTracksTrackDto> trackMappingResult,
        LastfmApiDtoProcessingResult<LastfmArtist, TagTopTracksTrackArtistDto> artistMappingResult,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTrack> relations = trackMappingResult.entityMapping().values().stream()
            .map(trackMapping -> {
                String artistName = trackMapping.getDto().getArtist().getName();
                EntityMapping<LastfmArtist, TagTopTracksTrackArtistDto> artistMapping =
                    artistMappingResult.entityMapping().get(artistName);
                if (artistMapping == null) {
                    log.warn("Artist not found for track {} - {}", artistName, trackMapping.getDto().getName());
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
        List<LastfmArtistTrack> lastfmArtistTracks = artistTrackService.upsertAll(relations);
        log.info("saved {} artist-track relations", lastfmArtistTracks.size());
    }

    private List<TagTopTracksTrackDto> getTrackDtos(TagTopTracksDtoRoot dtoRoot) {
        return dtoRoot.getRootObject().getTracks().stream()
                .peek(logMissingAttributes(dtoRoot))
                .filter(t -> t.getUrl() != null)
            .toList();
    }

    private List<TagTopTracksTrackArtistDto> getArtistDtos(TagTopTracksDtoRoot dtoRoot) {
        return getTrackDtos(dtoRoot).stream()
            .map(TagTopTracksTrackDto::getArtist)
            .filter(Objects::nonNull)
            .distinct()
            .toList();
    }

    private Consumer<TagTopTracksTrackDto> logMissingAttributes(final TagTopTracksDtoRoot dtoRoot) {
        return (dto) -> {
            if (!rootsWithMissingAttrsLogged.contains(dtoRoot)) {
                rootsWithMissingAttrsLogged.add(dtoRoot);
                if (dto.getUrl() == null) {
                    log.error("Url is null in track {}:{} by artist {}", dto.getName(), dto.getMbid(), dto.getArtist());
                }
                if (dto.getArtist() == null) {
                    log.warn("Artist is null in track {}:{}:{}", dto.getName(), dto.getMbid(), dto.getUrl());
                }
            }
        };
    }
}
