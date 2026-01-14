package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityMappingResult;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.DtoQualityService;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.topartists.TagTopArtistsArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.topartists.TagTopArtistsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTagService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistTagService;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LastfmTagTopArtistsResponseProcessor extends LastfmApiResponseProcessor<TagTopArtistsDtoRoot> {

    private final LastfmArtistService artistService;
    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
    private final DtoQualityService dtoQualityService;
    private final EntityFactory<LastfmArtist, TagTopArtistsArtistDto> artistFactory;
    private final LastfmTagService tagService;
    private final LastfmArtistTagService artistTagService;

    protected LastfmTagTopArtistsResponseProcessor(
        LastfmArtistService artistService,
        LastfmTagService tagService,
        LastfmArtistTagService artistTagService,
        EntityFactory<LastfmArtist, TagTopArtistsArtistDto> artistFactory,
        LastfmApiDtoProcessingOrchestrator dtoProcessingService,
        DtoQualityService dtoQualityService,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(TagTopArtistsDtoRoot.class, objectMapper);

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
