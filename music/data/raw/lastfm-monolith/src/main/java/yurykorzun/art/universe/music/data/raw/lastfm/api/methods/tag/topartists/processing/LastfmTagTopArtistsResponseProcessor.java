package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.processing;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiDtoProcessingService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmTagService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopArtistsResponseProcessor extends LastfmApiResponseProcessor<TagTopArtistsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingService dtoProcessingService;
    private final DtoQualityService dtoQualityService;
    private final EntityFactory<LastfmArtist, TagTopArtistsArtistDto> artistFactory;
    private final LastfmTagService tagService;
    private final LastfmArtistTagService artistTagService;

    protected LastfmTagTopArtistsResponseProcessor(
        LastfmArtistService artistService,
        LastfmTagService tagService,
        LastfmArtistTagService artistTagService,
        EntityFactory<LastfmArtist, TagTopArtistsArtistDto> artistFactory,
        LastfmApiDtoProcessingService dtoProcessingService,
        DtoQualityService dtoQualityService
    ) {
        super(TagTopArtistsDtoRoot.class);

        this.artistService = artistService;
        this.artistFactory = artistFactory;
        this.tagService = tagService;
        this.dtoProcessingService = dtoProcessingService;
        this.artistTagService = artistTagService;
        this.dtoQualityService = dtoQualityService;
    }

    private static final List<EntityAttributeHandler<LastfmArtist, ?, TagTopArtistsArtistDto>> attrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, TagTopArtistsArtistDto> factory = new EntityAttributeHandlerFactory<>(LastfmArtist.class, TagTopArtistsArtistDto.class);
        attrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID, false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url")
        );
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {

        TagTopArtistsDtoRoot dtoRoot = parseResponse(sourceApiResponse);
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        LastfmTag sourceTag = tagService.findById(sourceApiCall.getEntityId())
                .orElseThrow(() -> new EntityNotFoundException(String.format("Source tag with ID=%s not found", sourceApiCall.getEntityId())));

        var artistsMappingResult = updateArtists(dtoRoot, sourceApiCall);

        bindArtistsToTag(artistsMappingResult.entityMapping(), sourceTag, sourceApiCall);
    }

    private LastfmApiDtoProcessingResult<LastfmArtist, TagTopArtistsArtistDto> updateArtists(
        TagTopArtistsDtoRoot dtoRoot,
        LastfmApiCall sourceApiCall
    ) {
        List<TagTopArtistsArtistDto> allArtistDtos = dtoRoot.getTopArtists().getArtists();
        
        // Validate artists against blacklist
        var qualityArtistDtos = dtoQualityService.validateAgainstBlacklist(allArtistDtos)
            .stream()
            .filter(DtoQualityService.Result::isAccepted)
            .map(DtoQualityService.Result::getDto)
            .toList();

        if (qualityArtistDtos.size() < allArtistDtos.size()) {
            log.info("Filtered out {} blacklisted artists from tag's top artists", 
                allArtistDtos.size() - qualityArtistDtos.size());
        }

        var result = dtoProcessingService.process(
            sourceApiCall,
            qualityArtistDtos,
            artistFactory,
            attrHandlers,
            artistService
        );
        log.info("saved {} tag's artists", result.actualEntities().size());
        log.info("saved {} tag's artists' attributes", result.savedAttributeRecordsCount());

        return result;
    }

    private void bindArtistsToTag(
        EntityMappingResult<LastfmArtist, TagTopArtistsArtistDto> artistMappingResult,
        LastfmTag sourceTag,
        LastfmApiCall sourceApiCall
    ) {
        List<LastfmArtistTag> relations = artistMappingResult.getMap().values().stream()
            .map(artistMapping -> LastfmArtistTag.builder()
                    .apiCall(sourceApiCall)
                    .tag(sourceTag)
                    .artist(artistMapping.getNewEntity())
                .build())
            .collect(Collectors.toList());
        artistTagService.upsertAll(relations);
        log.info("saved {} artist-tag relations", relations.size());
    }
}
