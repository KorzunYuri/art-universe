package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmTagTopTagResponseProcessor extends LastfmApiResponseProcessor<TagTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmTag, TagTopTagsTagDto> tagFactory;

    private static final List<EntityAttributeHandler<LastfmTag, ?, TagTopTagsTagDto>> attrHandlers = List.of(
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  false,
            TagTopTagsTagDto::getRank),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.RELATIONS_COUNT,  false,
            LastfmTag::getUsageCount, LastfmTag::setUsageCount, TagTopTagsTagDto::getCount),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.REACH,  false,
            LastfmTag::getUsageUsersCount, LastfmTag::setUsageUsersCount, TagTopTagsTagDto::getReach)
    );

    protected LastfmTagTopTagResponseProcessor(
        LastfmTagService tagService,
        LastfmApiDtoProcessingService dtoProcessingService,
        EntityFactory<LastfmTag, TagTopTagsTagDto> tagFactory)
    {
        super(TagTopTagsDtoRoot.class);

        this.tagService = tagService;
        this.dtoProcessingService = dtoProcessingService;
        this.tagFactory = tagFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TagTopTagsDtoRoot dtoRoot = parseResponse(response);
        List<TagTopTagsTagDto> dtos = dtoRoot.getTopTags().getTags();

        // add 'rank' attribute
        PageInfo pageInfo = dtoRoot.getTopTags().getPageInfo();
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRank(pageInfo.getOffset() + i + 1);
        }

        List<String> tagNames = dtos.stream().map(TagDto::getName).toList();
        List<LastfmTag> existingTags = tagService.findAllByNameIn(tagNames);

        LastfmApiDtoProcessingResult<LastfmTag> result = dtoProcessingService.processDtosWithoutRelations(
            dtos, existingTags, response,
            tagFactory, attrHandlers, tagService::saveTags
        );
        log.info("saved {} top tags", result.savedEntities().size());
        log.info("saved {} top tags' attributes", result.savedAttributeValues().size());

    }

}