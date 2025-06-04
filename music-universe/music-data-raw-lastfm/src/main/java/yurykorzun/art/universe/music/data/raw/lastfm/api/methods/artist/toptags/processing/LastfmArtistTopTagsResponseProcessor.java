package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopTagsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final LastfmEntityRelationService entityRelationService;
    private final EntityFactory<LastfmTag, ArtistTopTagsTagDto> tagEntityFactory;

    @Value("${lastfm.client.methods.artist.topTags.tagUsageCountThreshold:10}")
    private int tagUsageCountThreshold;

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistTopTagsTagDto>> tagAttrHandlers;
    static {
        tagAttrHandlers = List.of(
            new EntityAttributeHandlerFactory<>(LastfmTag.class, ArtistTopTagsTagDto.class)
                .createHandler(LastfmAttribute.URL, false, "url"),
            DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.REACH, true,
                ArtistTopTagsTagDto::getUsageCount),
            DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK, true,
                ArtistTopTagsTagDto::getRank)
        );
    }

    protected LastfmArtistTopTagsResponseProcessor(
        LastfmTagService tagService,
        LastfmArtistService artistService,
        LastfmApiDtoProcessingService dtoProcessingService,
        LastfmEntityRelationService entityRelationService,
        EntityFactory<LastfmTag, ArtistTopTagsTagDto> tagEntityFactory
    ) {
        super(ArtistTopTagsDtoRoot.class);

        this.tagService = tagService;
        this.artistService = artistService;
        this.dtoProcessingService = dtoProcessingService;
        this.entityRelationService = entityRelationService;
        this.tagEntityFactory = tagEntityFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopTagsDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        LastfmApiDtoProcessingResult<LastfmTag> tagsUpdateResult = updateTags(sourceApiResponse, dtoRoot);

        bingTagsToArtist(tagsUpdateResult.savedEntities(), sourceApiResponse);
    }

    private void bingTagsToArtist(List<LastfmTag> tags, LastfmApiResponse sourceApiResponse) {
        long artistId = sourceApiResponse.getApiCall().getEntityId();
        Optional<LastfmArtist> artist = artistService.findById(artistId);
        if (artist.isPresent()) {
            bindTagsToArtist(artist.get(), tags, sourceApiResponse.getApiCall());
        } else {
            log.warn("artist with id {} not found", artistId);
        }
    }

    private LastfmApiDtoProcessingResult<LastfmTag> updateTags(LastfmApiResponse sourceApiResponse, ArtistTopTagsDtoRoot dtoRoot) {

        List<ArtistTopTagsTagDto> tagDtos = dtoRoot.getTopTagsObject().getTags();

        // calculate rank manually
        for (int i = 0; i < tagDtos.size(); i++) {
            tagDtos.get(i).setRank(i + 1);
        }

        // filter out dtos for saving
        tagDtos = filterDtosForSaving(tagDtos);

        // find existing entities
        List<String> tagNames = tagDtos.stream().map(ArtistTopTagsTagDto::getName).toList();
        List<LastfmTag> existingTags = tagService.findAllByNameIn(tagNames);

        // update entities from dtos
        LastfmApiDtoProcessingResult<LastfmTag> result = dtoProcessingService.processDtosWithoutRelations(
            tagDtos, existingTags, sourceApiResponse,
            tagEntityFactory,
            tagAttrHandlers,
            tagService::saveTags
        );
        log.info("saved {} artist's tags", result.savedEntities().size());
        log.info("saved {} artist's tags' attributes", result.savedAttributeValues().size());

        return result;
    }

    private List<ArtistTopTagsTagDto> filterDtosForSaving(List<ArtistTopTagsTagDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getUsageCount() >= tagUsageCountThreshold)
            .toList();
    }

    private void bindTagsToArtist(LastfmArtist artist, List<LastfmTag> lastfmTags, LastfmApiCall sourceApiCall) {
        List<LastfmEntityRelation> relations = lastfmTags.stream()
            .map(tag -> LastfmEntityRelation.builder()
                .apiCall(sourceApiCall)
                .scopeEntityType(tag.getType())
                .scopeEntityId(tag.getId())
                .entityType(artist.getType())
                .entityId(artist.getId())
                .build())
            .collect(Collectors.toList());
        entityRelationService.upsertEntityRelations(relations);
        log.info("saved {} tag-artist relations", relations.size());
    }
}
