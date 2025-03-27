package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@Slf4j
public class LastfmTagTopTagResponseProcessor extends LastfmApiResponseProcessor<TopTagsDtoRoot> {

    private final LastfmTagRepository tagRepository;
    private final LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    protected LastfmTagTopTagResponseProcessor(
            LastfmTagRepository tagRepository,
            LastfmAttributeHistoryRecordRepository attributeHistoryRepository)
    {
        super(TopTagsDtoRoot.class);

        this.tagRepository = tagRepository;
        this.attributeHistoryRepository = attributeHistoryRepository;
    }

    @Override
    protected ApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @RequiredArgsConstructor
    private static class TagMapping {
        final LastfmApiResponse response;
        final TagDto dto;
        final int rank;
        long id = -1;
        boolean shouldBeSaved() {
            return id < 0;
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void processResponse(LastfmApiResponse response) throws IOException {

        TopTagsDtoRoot parsed = parseResponse(response);

        final String logPrefix = String.format("Lastfm %s response processing", LastfmApiCallType.TAG_TOP_TAGS.getMethod());
        log.info("{}: start processing DTO of type {} with {} records",
                logPrefix,
                parsed.getClass().getName(),
                parsed.getTopTags().getTags().size());

        Map<String, TagMapping> dtoMappingByName = createTagDtosMapping(parsed, response);

        //  assign ids to DTOs that already exist in DB
        List<LastfmTag> existingTags = tagRepository.findAllByNameIn(dtoMappingByName.keySet());
        log.info("{}: Tags existed: {}", logPrefix, existingTags.size());
        mapDtosToIds(dtoMappingByName, existingTags);

        //  save tags that are not in DB yet
        List<LastfmTag> tagsToSave = dtoMappingByName.values().stream()
                .filter(TagMapping::shouldBeSaved)
                .map(this::tagDtoToTag)
                .toList();
        List<LastfmTag> savedTags = tagRepository.saveAll(tagsToSave);
        log.info("\"{}: Tags saved  : {}", logPrefix, savedTags.size());

        //  finalize id mapping. At this point every DTO must have a corresponding tag in DB
        mapDtosToIds(dtoMappingByName, savedTags);

        //  convert attributes in DTO to entities
        List<LastfmAttributeHistoryRecord> attributesToSave = tagDtosToAttributes(dtoMappingByName);
        attributesToSave = attributeHistoryRepository.saveAll(attributesToSave);
        log.info("\"{}: Tag attributes saved: {}", logPrefix, attributesToSave.size());

        log.info("\"{}: Finished processing DTO of type {}", logPrefix, parsed.getClass().getName());
    }

    private Map<String, TagMapping> createTagDtosMapping(TopTagsDtoRoot tagsDtoRoot, LastfmApiResponse response) {
        List<TagDto> tagDtos = tagsDtoRoot.getTopTags().getTags();
        PageInfo pageInfo = tagsDtoRoot.getTopTags().getPageInfo();

        // create a structure to retrieve DTO and corresponding tag id by name, which temporarily serves as PK
        Map<String, TagMapping> dtoMappingByName = new HashMap<>();
        for (int i = 0; i < tagDtos.size(); i++) {
            TagDto tagDto = tagDtos.get(i);
            dtoMappingByName.put(
                    tagDto.getName(),
                    new TagMapping(response, tagDto, pageInfo.getOffset() + i + 1));
        }
        return dtoMappingByName;
    }

    private LastfmTag tagDtoToTag(TagMapping tagMapping) {
        return LastfmTag.builder()
                .name(tagMapping.dto.getName())
                .apiCall(tagMapping.response.getApiCall())
            .build();
    }

    private void mapDtosToIds(Map<?, TagMapping> dtoMapping, List<LastfmTag> persistedTags) {
        persistedTags.forEach(t -> {
            dtoMapping.get(t.getName()).id = t.getId();
        });
    }

    private List<LastfmAttributeHistoryRecord> tagDtosToAttributes(Map<?, TagMapping> dtoMapping) {
        return dtoMapping.values().parallelStream()
                .flatMap(this::tagDtoToAttributes)
            .toList();
    }

    /**
     * Convert tag DTO to a stream of tag attributes
     *
     * @param tagDtoInfo {@link TagDto} wrapper containing tag id and additional info
     * @return {@link Stream} of {@link LastfmAttributeHistoryRecord} bound to the tag represented by the DTO
     */
    private Stream<LastfmAttributeHistoryRecord> tagDtoToAttributes(TagMapping tagDtoInfo) {
        return Stream.of(
                initTagBuilder(tagDtoInfo)
                        .attribute(LastfmAttribute.RELATIONS_COUNT)
                        .intValue(tagDtoInfo.dto.getCount())
                    .build(),
                initTagBuilder(tagDtoInfo)
                        .attribute(LastfmAttribute.REACH)
                        .intValue(tagDtoInfo.dto.getReach())
                    .build(),
                initTagBuilder(tagDtoInfo)
                        .attribute(LastfmAttribute.RANK)
                        .intValue(tagDtoInfo.rank)
                    .build()
        );
    }

    /**
     * Initialize {@link LastfmAttributeHistoryRecord} builder with values shared across all instances to reduce code duplication.
     *
     * @param tagDtoInfo tag DTO wrapper containing tag id and additional info
     * @return instance of {@link LastfmAttributeHistoryRecord} bulder
     */
    private LastfmAttributeHistoryRecord.LastfmAttributeHistoryRecordBuilder initTagBuilder(TagMapping tagDtoInfo) {
        return LastfmAttributeHistoryRecord.builder()
                .apiCallId(tagDtoInfo.response.getApiCall().getId())
                .entityType(LastfmEntityType.TAG)
                .entityId(tagDtoInfo.id);
    }
}