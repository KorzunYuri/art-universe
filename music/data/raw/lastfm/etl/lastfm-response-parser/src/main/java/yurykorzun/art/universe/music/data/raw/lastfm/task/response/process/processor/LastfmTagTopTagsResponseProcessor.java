package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags.TagTopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags.TagTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmTagTopTagsResponseProcessor extends LastfmApiResponseProcessor<TagTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
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
        LastfmApiDtoProcessingOrchestrator dtoProcessingService,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    )
    {
        super(TagTopTagsDtoRoot.class, objectMapper);

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