package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsRootDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.DefaultEntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service.LastfmTagService;

import java.io.IOException;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopTagsResponseProcessor extends LastfmApiResponseProcessor<ArtistTopTagsRootDto> {

    private final LastfmTagService tagService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final EntityFactory<LastfmTag, ArtistTopTagsTagDto> tagEntityFactory;

    @Value("${lastfm.client.methods.artist.topTags.tagUsageCountThreshold:10}")
    private int tagUsageCountThreshold;

    private static final List<EntityAttributeHandler<LastfmTag, ?, ArtistTopTagsTagDto>> tagAttrHandlers = List.of(
        DefaultEntityAttributeHandler.forEmbeddedAttribute(LastfmAttribute.URL, false,
            LastfmTag::getUrl, LastfmTag::setUrl, ArtistTopTagsTagDto::getUrl),
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.REACH, true,
            ArtistTopTagsTagDto::getUsageCount),
        DefaultEntityAttributeHandler.forExternalAttribute(LastfmAttribute.RANK, true,
            ArtistTopTagsTagDto::getRank)
    );

    protected LastfmArtistTopTagsResponseProcessor(
        LastfmTagService tagService,
        LastfmApiDtoProcessingService dtoProcessingService
    ) {
        super(ArtistTopTagsRootDto.class);

        this.tagService = tagService;
        this.dtoProcessingService = dtoProcessingService;

        this.tagEntityFactory = new LastfmArtistTopTagsTagFactory();
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        ArtistTopTagsRootDto dtoRoot = parseResponse(sourceApiResponse);

        updateTags(sourceApiResponse, dtoRoot);
    }

    private void updateTags(LastfmApiResponse sourceApiResponse, ArtistTopTagsRootDto dtoRoot) {

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
        LastfmApiDtoProcessingResult<LastfmTag> result = dtoProcessingService.processDtosWithRelations(
            tagDtos, existingTags, sourceApiResponse,
            tagEntityFactory,
            tagAttrHandlers,
            tagService::saveTags
        );
        log.info("saved {} artist's tags", result.savedEntities().size());
        log.info("saved {} artist's tags' attributes", result.savedAttributeValues().size());
        log.info("saved {} artist-tag relations", result.savedEntityRelations().size());
    }

    private List<ArtistTopTagsTagDto> filterDtosForSaving(List<ArtistTopTagsTagDto> dtos) {
        return dtos.stream()
            .filter(dto -> dto.getUsageCount() >= tagUsageCountThreshold)
            .toList();
    }
}
