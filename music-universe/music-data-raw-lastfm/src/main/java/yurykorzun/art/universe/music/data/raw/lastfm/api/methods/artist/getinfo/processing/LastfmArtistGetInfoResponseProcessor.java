package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.LastfmArtistEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoArtistTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto.ArtistGetInfoDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityRelationBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultAttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.DefaultEntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
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

    private final String logPrefix = String.format("Lastfm %s response processing", getApiCallType().getMethod());
    private final LastfmArtistService artistService;
    private final LastfmTagService tagService;
    private final LastfmEntityRelationService entityRelationService;
    private final LastfmAttributeHistoryService attributeHistoryService;

    private final LastfmArtistEntityFactory<ArtistGetInfoArtistDto> artistFactory;
    private final LastfmArtistEntityFactory<ArtistDto> similarArtistFactory;
    private final LastfmTagEntityFactory<ArtistGetInfoArtistTagDto> tagFactory;

    protected LastfmArtistGetInfoResponseProcessor(
        LastfmArtistService artistService,
        LastfmTagService tagService,
        LastfmEntityRelationService entityRelationService,
        LastfmAttributeHistoryService attributeHistoryService
    ) {
        super(ArtistGetInfoDtoRoot.class);

        this.artistService = artistService;
        this.tagService = tagService;
        this.entityRelationService = entityRelationService;
        this.attributeHistoryService = attributeHistoryService;

        this.artistFactory = new LastfmArtistGetInfoArtistFactory();
        this.similarArtistFactory = new LastfmArtistEntityFactory<>();
        this.tagFactory = new LastfmArtistGetInfoTagFactory();
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistGetInfoArtistDto>> artistAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.MBID,  false,
            LastfmArtist::getMbid, LastfmArtist::setMbid, ArtistGetInfoArtistDto::getMbid),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, ArtistGetInfoArtistDto::getUrl),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_STREAMABLE,  false,
            LastfmArtist::getIsStreamable, LastfmArtist::setIsStreamable,
            (dto) -> 1 == dto.getStreamable()),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.IS_ON_TOUR,  false,
            LastfmArtist::getIsOnTour, LastfmArtist::setIsOnTour,
            (dto) -> 1 == dto.getOnTour()),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.LISTENERS_COUNT, false,
            LastfmArtist::getListenersCount, LastfmArtist::setListenersCount,
            (dto) -> dto.getStats().getListeners()),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.PLAY_COUNT, false,
            LastfmArtist::getPlayCount, LastfmArtist::setPlayCount,
            (dto) -> dto.getStats().getPlayCount())
    );

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistDto>> similarArtistAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmArtist::getUrl, LastfmArtist::setUrl, ArtistDto::getUrl)
    );

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistGetInfoArtistTagDto>> tagAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmTag::getUrl, LastfmTag::setUrl, ArtistGetInfoArtistTagDto::getUrl)
    );

    @Override
    protected ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistGetInfoDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        log.info("{}: start processing DTO of type {}", logPrefix, dtoRoot.getClass().getName());

        LastfmArtist artist = updateArtist(dtoRoot, sourceApiResponse);

        Map<String, LastfmArtist> artistMap = updateSimilarArtists(dtoRoot, sourceApiResponse);

        Map<String, LastfmTag> tagMap = updateTags(dtoRoot, sourceApiResponse);

        bindArtistsToArtist(artist, artistMap, sourceApiResponse.getApiCall());

        bindTagsToArtist(artist, tagMap, sourceApiResponse.getApiCall());
    }

    private LastfmArtist updateArtist(ArtistGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        LastfmApiDtoProcessor<LastfmArtist, ArtistGetInfoArtistDto> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );
        ArtistGetInfoArtistDto dto = dtoRoot.getArtist();
        Optional<LastfmArtist> artist = artistService.findByName(dto.getName());

        LastfmApiDtoProcessingResult<LastfmArtist> result = mappingService.processDtos(
            List.of(dto), artist.stream().toList(), sourceApiResponse,
            artistFactory,
            artistAttrHandlers,
            artistService::saveArtists,
            attributeHistoryService::upsertCandidateValues
        );

        if (result.savedEntities().size() == 1) {
            return result.savedEntities().get(0);
        } else if (artist.isPresent()) {
            return artist.get();
        } else {
            throw new IllegalArgumentException(String.format("Artist %s neither existed in DB nor was created", dto.getName()));
        }
    }

    private Map<String, LastfmArtist> updateSimilarArtists(ArtistGetInfoDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {

        List<ArtistDto> artistDtos = dtoRoot.getArtist().getSimilarArtistsObject().getArtists();
        List<String> artistNames = artistDtos.stream().map(ArtistDto::getName).toList();
        List<LastfmArtist> existingArtists = artistService.findAllByNames(artistNames);

        LastfmApiDtoProcessor<LastfmArtist, ArtistDto> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );

        LastfmApiDtoProcessingResult<LastfmArtist> result = mappingService.processDtos(
            artistDtos, existingArtists, sourceApiResponse,
            similarArtistFactory,
            similarArtistAttrHandlers,
            artistService::saveArtists,
            attributeHistoryService::upsertCandidateValues
        );
        log.info("{}: saved {} similar artists", logPrefix, result.savedEntities().size());

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

        LastfmApiDtoProcessor<LastfmTag, ArtistGetInfoArtistTagDto> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );
        LastfmApiDtoProcessingResult<LastfmTag> result = mappingService.processDtos(dtos, existingTags, sourceApiResponse,
            tagFactory,
            tagAttrHandlers,
            tagService::saveTags,
            attributeHistoryService::upsertCandidateValues
        );

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
        log.info("{}: saved {} artist-artist relations", logPrefix, relations.size());
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
        log.info("{}: saved {} tag-artist relations", logPrefix, relations.size());
    }

}
