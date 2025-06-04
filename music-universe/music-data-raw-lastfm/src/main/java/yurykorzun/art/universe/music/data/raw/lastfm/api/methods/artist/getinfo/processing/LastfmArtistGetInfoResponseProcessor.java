package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistGetInfoResponseProcessor extends LastfmApiResponseProcessor<ArtistGetInfoDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmEntityRelationService entityRelationService;

    private final EntityFactory<LastfmArtist, ArtistGetInfoArtistDto> artistFactory;
    private final EntityFactory<LastfmArtist, ArtistGetInfoSimilarArtistDto> similarArtistFactory;
    private final EntityFactory<LastfmTag, ArtistGetInfoArtistTagDto> tagFactory;

    protected LastfmArtistGetInfoResponseProcessor(
        LastfmArtistService artistService,
        LastfmTagService tagService,
        LastfmApiDtoProcessingService dtoProcessingService,
        LastfmEntityRelationService entityRelationService,
        EntityFactory<LastfmArtist, ArtistGetInfoArtistDto> artistFactory,
        EntityFactory<LastfmArtist, ArtistGetInfoSimilarArtistDto> similarArtistFactory,
        EntityFactory<LastfmTag, ArtistGetInfoArtistTagDto> tagFactory
    ) {
        super(ArtistGetInfoDtoRoot.class);

        this.artistService = artistService;
        this.tagService = tagService;
        this.dtoProcessingService = dtoProcessingService;
        this.entityRelationService = entityRelationService;
        this.artistFactory = artistFactory;
        this.similarArtistFactory = similarArtistFactory;
        this.tagFactory = tagFactory;
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetInfoArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistGetInfoArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistGetInfoArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.IS_STREAMABLE, false, "isStreamable",
                (dto) -> 1 == dto.getStreamable()),
            factory.createHandler(LastfmAttribute.IS_ON_TOUR, false, "isOnTour",
                (dto) -> 1 == dto.getOnTour()),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount",
                (dto) -> dto.getStats().getListeners()),
            factory.createHandler(LastfmAttribute.PLAY_COUNT, false, "playCount",
                (dto) -> dto.getStats().getPlayCount())
        );
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetInfoSimilarArtistDto>> similarArtistAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistGetInfoSimilarArtistDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistGetInfoArtistTagDto>> tagAttrHandlers = List.of(
        new EntityAttributeHandlerFactory<>(LastfmTag.class, ArtistGetInfoArtistTagDto.class)
            .createHandler(LastfmAttribute.URL, false, "url")
    );

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        
        ArtistGetInfoDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        // update source artists
        LastfmArtist artist = updateArtist(dtoRoot, sourceApiResponse);

        // update artist's tags
        Map<String, LastfmTag> tagMap = updateTags(dtoRoot, sourceApiResponse);

        // bind artist's tags to artist
        bindTagsToArtist(artist, tagMap, sourceApiResponse.getApiCall());
    }

    private LastfmArtist updateArtist(ArtistGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        ArtistGetInfoArtistDto dto = dtoRoot.getArtist();
        Optional<LastfmArtist> artist = artistService.findByName(dto.getName());

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithoutRelations(
            List.of(dto), artist.stream().toList(), sourceApiResponse,
            artistFactory,
            artistAttrHandlers,
            artistService::saveArtists
        );
        log.info("saved artist {}", dto.getName());
        log.info("saved {} artists' attributes", result.savedAttributeValues().size());

        if (result.savedEntities().size() == 1) {
            return result.savedEntities().get(0);
        } else if (artist.isPresent()) {
            return artist.get();
        } else {
            throw new IllegalArgumentException(String.format("Artist %s neither existed in DB nor was created", dto.getName()));
        }
    }

    private Map<String, LastfmArtist> updateSimilarArtists(ArtistGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        List<ArtistGetInfoSimilarArtistDto> artistDtos = dtoRoot.getArtist().getSimilarArtistsObject().getArtists();
        List<String> artistNames = artistDtos.stream().map(ArtistDto::getName).toList();
        List<LastfmArtist> existingArtists = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessingResult<LastfmArtist> result = dtoProcessingService.processDtosWithoutRelations(
            artistDtos, existingArtists, sourceApiResponse,
            similarArtistFactory,
            similarArtistAttrHandlers,
            artistService::saveArtists
        );
        log.info("saved {} artist's similar artists", result.savedEntities().size());
        log.info("saved {} artist's similar artists' attributes", result.savedAttributeValues().size());

        //  merge existing and new artists to eliminate the second call to database for artists
        Map<String, LastfmArtist> artistMap = existingArtists.stream()
            .collect(Collectors.toMap(LastfmArtist::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(a -> artistMap.putIfAbsent(a.getUniqueKey(), a));

        return artistMap;
    }

    private Map<String, LastfmTag> updateTags(ArtistGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        List<ArtistGetInfoArtistTagDto> dtos = dtoRoot.getArtist().getTagsObject().getTags();
        List<String> tagNames = dtos.stream().map(TagDto::getName).toList();
        List<LastfmTag> existingTags = tagService.findAllByNameIn(tagNames);

        LastfmApiDtoProcessingResult<LastfmTag> result = dtoProcessingService.processDtosWithoutRelations(
            dtos, existingTags, sourceApiResponse,
            tagFactory, tagAttrHandlers, tagService::saveTags
        );
        log.info("saved {} tags", result.savedEntities().size());
        log.info("saved {} tags' attributes", result.savedAttributeValues().size());

        //  merge existing and new artists to eliminate the second call to database for artists
        Map<String, LastfmTag> tagsMap = existingTags.stream()
            .collect(Collectors.toMap(LastfmTag::getUniqueKey, Function.identity()));
        result.savedEntities().forEach(a -> tagsMap.putIfAbsent(a.getUniqueKey(), a));

        return tagsMap;
    }

    private void bindArtistsToArtist(LastfmArtist artist, Map<String, LastfmArtist> artistMap, LastfmApiCall sourceApiCall) {
        List<LastfmEntityRelation> relations = artistMap.values().stream()
            .map((similarArtist) -> LastfmEntityRelation.builder()
                    .apiCall(sourceApiCall)
                    .scopeEntityType(LastfmEntityType.ARTIST)
                    .scopeEntityId(artist.getId())
                    .entityType(LastfmEntityType.ARTIST)
                    .entityId(similarArtist.getId())
                .build())
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("saved {} artist-artist relations", relations.size());
    }

    private void bindTagsToArtist(LastfmArtist artist, Map<String, LastfmTag> tagMap, LastfmApiCall sourceApiCall) {
        List<LastfmEntityRelation> relations = tagMap.values().stream()
            .map((tag) -> LastfmEntityRelation.builder()
                    .apiCall(sourceApiCall)
                    .scopeEntityType(LastfmEntityType.TAG)
                    .scopeEntityId(tag.getId())
                    .entityType(LastfmEntityType.ARTIST)
                    .entityId(artist.getId())
                .build())
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("saved {} tag-artist relations", relations.size());
    }

}
