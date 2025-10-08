package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmTagTopTagsResponseProcessor extends LastfmApiResponseProcessor<TagTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmTag, TagTopTagsTagDto> tagFactory;

    private static final List<EntityAttributeHandler<LastfmTag, ?, TagTopTagsTagDto>> attrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmTag, TagTopTagsTagDto> factory = new EntityAttributeHandlerFactory<>(LastfmTag.class, TagTopTagsTagDto.class);
        attrHandlers = List.of(
            factory.createHandler(LastfmAttribute.RELATIONS_COUNT,  false, "usageCount"),
            factory.createHandler(LastfmAttribute.USAGE_COUNT,  false, "usageUsersCount")
        );
    }

    protected LastfmTagTopTagsResponseProcessor(
        LastfmTagService tagService,
        EntityFactory<LastfmTag, TagTopTagsTagDto> tagFactory,
        LastfmApiDtoProcessingService dtoProcessingService
    )
    {
        super(TagTopTagsDtoRoot.class);

        this.tagService = tagService;
        this.tagFactory = tagFactory;
        this.dtoProcessingService = dtoProcessingService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopTagsDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();

        updateTags(dtoRoot, sourceApiCall);

    }

    private void updateTags(TagTopTagsDtoRoot dtoRoot, LastfmApiCall sourceApiCall) {
        List<TagTopTagsTagDto> dtos = dtoRoot.getTopTags().getTags();

        // add 'rank' attribute
        PageInfo pageInfo = dtoRoot.getTopTags().getPageInfo();
        for (int i = 0; i < dtos.size(); i++) {
            dtos.get(i).setRank(pageInfo.getOffset() + i + 1);
        }

        var result = dtoProcessingService.process(
            sourceApiCall,
            dtos,
            tagFactory,
            attrHandlers,
            tagService
        );
        log.info("saved {} top tags", result.actualEntities().size());
        log.info("saved {} top tags' attributes", result.savedAttributeRecordsCount());
    }

}