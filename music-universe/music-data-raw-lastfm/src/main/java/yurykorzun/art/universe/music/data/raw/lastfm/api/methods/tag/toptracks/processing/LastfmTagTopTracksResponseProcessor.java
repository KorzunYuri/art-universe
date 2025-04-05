package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityRelationBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultAttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.DefaultEntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto.TagTopTracksDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.LastfmTrackEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
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
    private final LastfmEntityRelationService entityRelationService;
    private final LastfmAttributeHistoryService attributeHistoryService;

    private Set<TagTopTracksDtoRoot> rootsWithMissingAttrsLogged = new HashSet<>();

    protected LastfmTagTopTracksResponseProcessor(
        LastfmArtistService artistService,
        LastfmTrackService lastfmTrackService,
        LastfmEntityRelationService entityRelationService,
        LastfmAttributeHistoryService attributeHistoryService
    ) {
        super(TagTopTracksDtoRoot.class);
        this.artistService = artistService;

        this.trackService = lastfmTrackService;
        this.entityRelationService = entityRelationService;
        this.attributeHistoryService = attributeHistoryService;
    }

    private static final List<EntityAttributeHandler<LastfmTrack, ?, TrackDto>> trackAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmTrack::getMbid, LastfmTrack::setMbid, TrackDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmTrack::getUrl, LastfmTrack::setUrl, TrackDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.DURATION, false,
            LastfmTrack::getDuration, LastfmTrack::setDuration, TrackDto::getDuration),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE, false,
            LastfmTrack::getStreamable, LastfmTrack::setStreamable,
            (dto) -> dto.getStreamableObject().getFullTrack())
        //DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  true,
        //    (dto) -> dto.getRankInfo().getRank())
    );

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistDto>> artistAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, ArtistDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, ArtistDto::getUrl)
    );

    @Override
    protected ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopTracksDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        List<TrackDto> trackDtos = getTrackDtos(dtoRoot);

        final String logPrefix = String.format("Lastfm %s response processing", getApiCallType().getMethod());
        log.info("{}: start processing DTO of type {} with {} records", logPrefix, dtoRoot.getClass().getName(), trackDtos.size());

        //  first save new artists
        Map<String, LastfmArtist> artistMap = updateArtists(dtoRoot, sourceApiResponse);

        //  then save new tracks
        LastfmApiDtoProcessor<LastfmTrack, TrackDto> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );

        List<String> trackUrls = getTrackUrls(trackDtos);
        List<LastfmTrack> existingTracks = trackService.findAllByUrls(trackUrls);

        LastfmApiDtoProcessingResult<LastfmTrack> result = mappingService.processDtos(trackDtos, existingTracks, sourceApiResponse,
            new LastfmTrackEntityFactory(), trackAttrHandlers,
            trackService::saveTracks,
            attributeHistoryService::upsertCandidateValues,
            entityRelationService::upsertEntityRelations
        );

        // merge existing and saved tracks to eliminate the second call to database for tracks
        Map<String, LastfmTrack> trackMap = existingTracks.stream()
            .collect(Collectors.toMap(LastfmTrack::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(t -> trackMap.putIfAbsent(t.getUniqueKey(), t));

        //  finally, bind all tracks to artists
        bindTracksToArtists(trackDtos, trackMap, artistMap, sourceApiResponse.getApiCall());

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, dtoRoot.getClass().getName());
    }

    /**
     * Extract artists from root dto and save them.
     * @return <b>ALL</b> the artist entities.
     */
    private Map<String, LastfmArtist> updateArtists(TagTopTracksDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        log.info("start processing artists contained in tracks");

        List<ArtistDto> artistDtos = getArtistDtos(dtoRoot);
        List<String> artistNames = getArtistNames(dtoRoot);
        List<LastfmArtist> existingArtists = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessor<LastfmArtist, ArtistDto> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );

        LastfmApiDtoProcessingResult<LastfmArtist> result = mappingService.processDtos(artistDtos, existingArtists, sourceApiResponse,
             new LastfmArtistEntityFactory(), artistAttrHandlers,
            artistService::saveArtists,
            attributeHistoryService::upsertCandidateValues
        );
        log.info("saved {} artists", result.savedEntities().size());

        //  merge existing and new artists to eliminate the second call to database for artists
        Map<String, LastfmArtist> artistMap = existingArtists.stream()
            .collect(Collectors.toMap(LastfmArtist::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(a -> artistMap.putIfAbsent(a.getUniqueKey(), a));

        return artistMap;
    }

    //  map tracks to artists
    private void bindTracksToArtists(
        List<TrackDto> trackDtos, Map<String, LastfmTrack> trackMap, Map<String, LastfmArtist> artistMap, LastfmApiCall sourceApiCall) {
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
    }

    private List<TrackDto> getTrackDtos(TagTopTracksDtoRoot dtoRoot) {
        return dtoRoot.getRootObject().getTracks().stream()
                .peek(logMissingAttributes(dtoRoot))
                .filter(t -> t.getUrl() != null)
            .toList();
    }

    private Consumer<TrackDto> logMissingAttributes(final TagTopTracksDtoRoot dtoRoot) {
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

    private List<String> getTrackUrls(List<TrackDto> trackDtos) {
        return trackDtos.stream().map(TrackDto::getUrl).toList();
    }

    private List<String> getArtistNames(TagTopTracksDtoRoot dtoRoot) {
        return getArtistNames(getArtistDtos(dtoRoot));
    }

    private List<String> getArtistNames(List<ArtistDto> artistDtos) {
        return artistDtos.stream().map(ArtistDto::getName).toList();
    }

    private List<ArtistDto> getArtistDtos(TagTopTracksDtoRoot dtoRoot) {
        return getTrackDtos(dtoRoot).stream()
                .map(TrackDto::getArtist)
                .filter(Objects::nonNull)
                .distinct()
            .toList();
    }

}
