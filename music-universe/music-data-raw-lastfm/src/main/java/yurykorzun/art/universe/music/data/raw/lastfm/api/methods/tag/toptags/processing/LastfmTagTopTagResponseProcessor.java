package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.LastfmTagEntityFactory;
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
// TODO rename
public class LastfmTagTopTagResponseProcessor extends LastfmApiResponseProcessor<TagTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingService dtoProcessingService;

    private static final List<EntityAttributeHandler<LastfmTag, ?, TagTopTagsTagDto>> attrHandlers = List.of(
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  false,
            TagTopTagsTagDto::getRank),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.RELATIONS_COUNT,  false,
            LastfmTag::getUsageCount, LastfmTag::setUsageCount, TagTopTagsTagDto::getCount),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.REACH,  false,
            LastfmTag::getUsageUsersCount, LastfmTag::setUsageUsersCount, TagTopTagsTagDto::getReach)
    );
    private final TagTopTagsTagFactory tagFactory;


    protected LastfmTagTopTagResponseProcessor(
        LastfmTagService tagService,
        LastfmApiDtoProcessingService dtoProcessingService)
    {
        super(TagTopTagsDtoRoot.class);

        this.tagService = tagService;
        this.dtoProcessingService = dtoProcessingService;

        this.tagFactory = new TagTopTagsTagFactory();
    }

    @Override
    protected ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TagTopTagsDtoRoot dtoRoot = parseResponse(response);
        List<TagTopTagsTagDto> dtos = dtoRoot.getTopTags().getTags();

        final String logPrefix = String.format("Lastfm %s response processing", getApiCallType().getMethod());
        log.info("{}: start processing DTO of type {} with {} records", logPrefix, dtoRoot.getClass().getName(), dtos.size());

        // add 'rank' attribute
        PageInfo pageInfo = dtoRoot.getTopTags().getPageInfo();
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRank(pageInfo.getOffset() + i + 1);
        }

        List<String> tagNames = dtos.stream().map(TagDto::getName).toList();
        List<LastfmTag> existingTags = tagService.findAllByNameIn(tagNames);

        dtoProcessingService.processDtosWithoutRelations(
            dtos, existingTags, response,
            tagFactory, attrHandlers, tagService::saveTags
        );

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, dtoRoot.getClass().getName());
    }

    private static class TagTopTagsTagFactory extends LastfmTagEntityFactory<TagTopTagsTagDto> {

        @Override
        protected LastfmTag.LastfmTagBuilder<?, ?> setExtensionFields(LastfmTag.LastfmTagBuilder<?, ?> builder, TagTopTagsTagDto dto) {
            return builder
                .usageCount(dto.getCount())
                .usageUsersCount(dto.getReach());
        }
    }
}