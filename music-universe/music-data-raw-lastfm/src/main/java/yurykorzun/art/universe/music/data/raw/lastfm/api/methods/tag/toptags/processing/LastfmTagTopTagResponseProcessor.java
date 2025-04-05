package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityRelationBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultAttributeHistoryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.persistence.DefaultEntityPersister;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TagDtoWrapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LastfmTagTopTagResponseProcessor extends LastfmApiResponseProcessor<TopTagsDtoRoot> {

    private final LastfmTagRepository tagRepository;
    private final LastfmAttributeHistoryService attributeHistoryService;

    private static final List<EntityAttributeHandler<LastfmTag, ?, TagDtoWrapper>> attrHandlers = List.of(
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK,  false,
            TagDtoWrapper::rank),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.RELATIONS_COUNT,  false,
            LastfmTag::getUsageCount, LastfmTag::setUsageCount,
            (w) -> w.dto().getCount()),
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.REACH,  false,
            LastfmTag::getUsageUsersCount, LastfmTag::setUsageUsersCount,
            (w) -> w.dto().getReach())
    );


    protected LastfmTagTopTagResponseProcessor(
        LastfmTagRepository tagRepository,
        LastfmAttributeHistoryService attributeHistoryService)
    {
        super(TopTagsDtoRoot.class);

        this.tagRepository = tagRepository;
        this.attributeHistoryService = attributeHistoryService;
    }

    @Override
    protected ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TopTagsDtoRoot dtoRoot = parseResponse(response);
        List<TagDto> dtos = dtoRoot.getTopTags().getTags();

        final String logPrefix = String.format("Lastfm %s response processing", getApiCallType().getMethod());
        log.info("{}: start processing DTO of type {} with {} records", logPrefix, dtoRoot.getClass().getName(), dtos.size());

        // wrap dtos to add 'rank' attribute
        List<TagDtoWrapper> dtoWrappers = new ArrayList<>();
        PageInfo pageInfo = dtoRoot.getTopTags().getPageInfo();
        for (int i = 0; i < dtos.size(); i++) {
            TagDto dto = dtos.get(i);
            dtoWrappers.add(new TagDtoWrapper(dto, pageInfo.getOffset() + i + 1));
        }

        List<LastfmTag> existingArtists = tagRepository.findAllByNameIn(dtos.stream()
            .map(dto -> dto.getName()).toList());

        LastfmApiDtoProcessor<LastfmTag, TagDtoWrapper> mappingService = new LastfmApiDtoProcessor<>(
            new EntityMappingBuilder<>(),
            new DefaultEntityPersister<>(),
            new DefaultAttributeHistoryBuilder<>(),
            new EntityRelationBuilder<>()
        );
        mappingService.processDtos(dtoWrappers, existingArtists, response,
            new LastfmTagEntityFactory(),
            attrHandlers,
            tagRepository::saveAll,
            attributeHistoryService::upsertCandidateValues
        );

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, dtoRoot.getClass().getName());
    }
}