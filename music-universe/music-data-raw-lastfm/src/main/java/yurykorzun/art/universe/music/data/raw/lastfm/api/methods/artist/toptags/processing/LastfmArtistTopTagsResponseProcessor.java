package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import jakarta.persistence.EntityNotFoundException;
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
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeHistoryService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmArtistTopTagsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTagsDtoRoot> {

    private final LastfmTagService tagService;
    private final LastfmArtistService artistService;
    private final LastfmArtistTagService artistTagService;
    private final LastfmAttributeHistoryService attributeHistoryService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final DtoQualityService dtoQualityService;
    private final EntityFactory<LastfmTag, ArtistTopTagsTagDto> tagEntityFactory;

    @Value("${lastfm.client.methods.artist.topTags.tagUsageCountThreshold:10}")
    private int tagUsageCountThreshold;

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistTopTagsTagDto>> tagAttrHandlers;
    static {
        tagAttrHandlers = List.of(
            new EntityAttributeHandlerFactory<>(LastfmTag.class, ArtistTopTagsTagDto.class)
                .createHandler(LastfmAttribute.URL, false, "url"),
            DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.USAGE_COUNT, true,
                ArtistTopTagsTagDto::getUsageCount)
        );
    }

    protected LastfmArtistTopTagsResponseProcessor(
        LastfmTagService tagService,
        LastfmArtistService artistService,
        LastfmArtistTagService artistTagService,
        LastfmAttributeHistoryService attributeHistoryService,
        EntityFactory<LastfmTag, ArtistTopTagsTagDto> tagEntityFactory,
        LastfmApiDtoProcessingService dtoProcessingService,
        DtoQualityService dtoQualityService
    ) {
        super(ArtistTopTagsDtoRoot.class);

        this.tagService = tagService;
        this.artistService = artistService;
        this.artistTagService = artistTagService;
        this.attributeHistoryService = attributeHistoryService;
        this.tagEntityFactory = tagEntityFactory;
        this.dtoProcessingService = dtoProcessingService;
        this.dtoQualityService = dtoQualityService;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopTagsDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmArtist artist = artistService.findById(sourceApiCall.getEntityId())
            .orElseThrow(() -> new EntityNotFoundException(String.format("Source artist with ID=%s not found", sourceApiCall.getEntityId())));

        var tagsUpdateResult = updateTags(dtoRoot, sourceApiCall);

        bindTagsToArtist(tagsUpdateResult, artist, sourceApiCall);
    }

    private LastfmApiDtoProcessingResult<LastfmTag, ArtistTopTagsTagDto> updateTags(
        ArtistTopTagsDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall
    ) {

        List<ArtistTopTagsTagDto> tagDtos = dtoRoot.getTopTagsObject().getTags();

        // filter out dtos for saving
        tagDtos = filterDtosForSaving(tagDtos);

        // update entities from dtos
        LastfmApiDtoProcessingResult<LastfmTag, ArtistTopTagsTagDto> result = dtoProcessingService.process(
            sourceApiCall,
            tagDtos,
            tagEntityFactory,
            tagAttrHandlers,
            tagService
        );
        log.info("saved {} artist's tags", result.actualEntities().size());
        log.info("saved {} artist's tags' attributes", result.savedAttributeValues().size());

        return result;
    }

    private List<ArtistTopTagsTagDto> filterDtosForSaving(List<ArtistTopTagsTagDto> dtos) {
        // First filter by usage count threshold
        List<ArtistTopTagsTagDto> thresholdFilteredTags = dtos.stream()
            .filter(dto -> dto.getUsageCount() >= tagUsageCountThreshold)
            .toList();

        // Then validate against blacklist
        var qualityTags = dtoQualityService.validateAgainstBlacklist(thresholdFilteredTags)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityTags.size() < dtos.size()) {
            log.info("Filtered {} tags: {} below usage count threshold, {} blacklisted",
                dtos.size() - qualityTags.size(),
                dtos.size() - thresholdFilteredTags.size(),
                thresholdFilteredTags.size() - qualityTags.size());
        }

        return qualityTags;
    }

    private void bindTagsToArtist(
        LastfmApiDtoProcessingResult<LastfmTag, ArtistTopTagsTagDto> tagMappingResult,
        LastfmArtist artist,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTag> relations = tagMappingResult.entityMapping().values().stream()
            .map(tagMapping -> LastfmArtistTag.builder()
                    .apiCall(sourceApiCall)
                    .artist(artist)
                    .tag(tagMapping.getNewEntity())
                    .usageCount(tagMapping.getDto().getUsageCount())
                .build())
            .collect(Collectors.toList());
        artistTagService.upsertAll(relations);
        log.info("saved {} artist-tag relations", relations.size());

        List<LastfmAttributeHistoryRecord> records = relations.stream()
            .map(relation -> LastfmAttributeHistoryRecord.builder()
                .apiCallId(sourceApiCall.getId())
                .scopeEntityType(LastfmEntityType.ARTIST)
                .scopeEntityId(relation.getArtist().getId())
                .entityType(LastfmEntityType.TAG)
                .entityId(relation.getTag().getId())
                .attribute(LastfmAttribute.USAGE_COUNT)
                .numericValue(Long.valueOf(relation.getUsageCount()))
                .build())
            .toList();
        attributeHistoryService.upsertCandidateValues(records);
    }
}
