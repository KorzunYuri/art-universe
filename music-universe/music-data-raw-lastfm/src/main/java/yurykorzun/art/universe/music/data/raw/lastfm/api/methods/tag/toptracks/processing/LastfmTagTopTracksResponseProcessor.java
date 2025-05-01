package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.processing.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopTracksResponseProcessor extends LastfmApiResponseProcessor<TagTopTracksDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmTrackService trackService;
    private final TagTopTracksTrackFactory trackFactory;
    private final LastfmEntityRelationService entityRelationService;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    private final EntityFactory<LastfmArtist, TagTopTracksTrackArtistDto> artistFactory;

    private final Set<TagTopTracksDtoRoot> rootsWithMissingAttrsLogged = new HashSet<>();

    protected LastfmTagTopTracksResponseProcessor(
        LastfmArtistService artistService,
        LastfmTrackService lastfmTrackService,
        LastfmEntityRelationService entityRelationService,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(TagTopTracksDtoRoot.class);

        this.artistService = artistService;
        this.trackService = lastfmTrackService;
        this.entityRelationService = entityRelationService;
        this.dtoProcessingService = dtoProcessingService;

        this.trackFactory = new TagTopTracksTrackFactory();
        this.artistFactory = new LastfmArtistEntityFactory<>();
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, TagTopTracksTrackDto>> trackAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmTrack::getMbid, LastfmTrack::setMbid, TagTopTracksTrackDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmTrack::getUrl, LastfmTrack::setUrl, TagTopTracksTrackDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.DURATION, false,
            LastfmTrack::getDuration, LastfmTrack::setDuration, TagTopTracksTrackDto::getDuration),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE, false,
            LastfmTrack::getStreamable, LastfmTrack::setStreamable,
            (dto) -> 1 == dto.getStreamableObject().getFullTrack())
        //DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  true,
        //    (dto) -> dto.getRankInfo().getRank())
    );

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TagTopTracksTrackArtistDto>> artistAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, TagTopTracksTrackArtistDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, TagTopTracksTrackArtistDto::getUrl)
    );

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        //  first save new artists
        Map<String, LastfmArtist> artistMap = updateArtists(dtoRoot, sourceApiResponse);

        //  then save new tracks
        Map<String, LastfmTrack> trackMap = updateTracks(dtoRoot, sourceApiResponse);

        //  finally, bind all tracks to artists
        bindTracksToArtists(dtoRoot, trackMap, artistMap, sourceApiResponse.getApiCall());

        //  clean cache
        rootsWithMissingAttrsLogged.remove(dtoRoot);
    }

    private Map<String, LastfmTrack> updateTracks(TagTopTracksDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        List<TagTopTracksTrackDto> trackDtos = getTrackDtos(dtoRoot);
        List<String> trackUrls = getTrackUrls(trackDtos);
        List<LastfmTrack> existingTracks = trackService.findAllByUrls(trackUrls);

        LastfmApiDtoProcessingResult<LastfmTrack> result = dtoProcessingService.processDtosWithRelations(
            trackDtos, existingTracks, sourceApiResponse,
            trackFactory, trackAttrHandlers, trackService::saveTracks
        );
        log.info("saved {} tag's tracks", result.savedEntities().size());
        log.info("saved {} tag's tracks' attributes", result.savedAttributeValues().size());
        log.info("saved {} tag-track relations", result.savedEntityRelations().size());

        // merge existing and saved tracks to eliminate the second call to database for tracks
        Map<String, LastfmTrack> trackMap = existingTracks.stream()
            .collect(Collectors.toMap(LastfmTrack::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(t -> trackMap.putIfAbsent(t.getUniqueKey(), t));

        return trackMap;
    }

    /**
     * Extract artists from root dto and save them.
     * @return <b>ALL</b> the artist entities.
     */
    private Map<String, LastfmArtist> updateArtists(TagTopTracksDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        List<TagTopTracksTrackArtistDto> artistDtos = getArtistDtos(dtoRoot);
        List<String> artistNames = getArtistNames(dtoRoot);
        List<LastfmArtist> existingArtists = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithoutRelations(
            artistDtos, existingArtists, sourceApiResponse,
            artistFactory, artistAttrHandlers, artistService::saveArtists
        );
        log.info("saved {} tag's tracks' artists", result.savedEntities().size());
        log.info("saved {} tag's tracks' artists' attributes", result.savedAttributeValues().size());

        //  merge existing and new artists to eliminate the second call to database for artists
        Map<String, LastfmArtist> artistMap = existingArtists.stream()
            .collect(Collectors.toMap(LastfmArtist::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(a -> artistMap.putIfAbsent(a.getUniqueKey(), a));

        return artistMap;
    }

    //  map tracks to artists
    private void bindTracksToArtists(
        TagTopTracksDtoRoot dtoRoot, Map<String, LastfmTrack> trackMap, Map<String, LastfmArtist> artistMap, LastfmApiCall sourceApiCall
    ) {
        List<TagTopTracksTrackDto> trackDtos = getTrackDtos(dtoRoot);
        List<LastfmEntityRelation> relations = trackDtos.stream()
            .map((dto) -> {
                LastfmTrack track = trackMap.get(dto.getUniqueKey());
                LastfmArtist artist = artistMap.get(dto.getArtist().getUniqueKey());
                return LastfmEntityRelation.builder()
                        .apiCall(sourceApiCall)
                        .scopeEntityType(LastfmEntityType.ARTIST)
                        .scopeEntityId(artist.getId())
                        .entityType(LastfmEntityType.TRACK)
                        .entityId(track.getId())
                    .build();
            })
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("saved {} artist-track relations", relations.size());
    }

    private List<TagTopTracksTrackDto> getTrackDtos(TagTopTracksDtoRoot dtoRoot) {
        return dtoRoot.getRootObject().getTracks().stream()
                .peek(logMissingAttributes(dtoRoot))
                .filter(t -> t.getUrl() != null)
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

    private List<String> getTrackUrls(TagTopTracksDtoRoot dtoRoot) {
        return getTrackUrls(getTrackDtos(dtoRoot));
    }

    private List<String> getTrackUrls(List<TagTopTracksTrackDto> trackDtos) {
        return trackDtos.stream().map(TagTopTracksTrackDto::getUrl).toList();
    }

    private List<String> getArtistNames(TagTopTracksDtoRoot dtoRoot) {
        return getArtistNames(getArtistDtos(dtoRoot));
    }

    private List<String> getArtistNames(List<TagTopTracksTrackArtistDto> artistDtos) {
        return artistDtos.stream().map(TagTopTracksTrackArtistDto::getName).toList();
    }

    private List<TagTopTracksTrackArtistDto> getArtistDtos(TagTopTracksDtoRoot dtoRoot) {
        return getTrackDtos(dtoRoot).stream()
                .map(TagTopTracksTrackDto::getArtist)
                .filter(Objects::nonNull)
                .distinct()
            .toList();
    }
}
