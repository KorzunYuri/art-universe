package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.processor;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.config.MappingConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.search.ArtistSearchArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.search.ArtistSearchDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiDtoProcessingOrchestrator;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.EntityFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandler;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping.attributes.EntityAttributeHandlerFactory;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.dedup.ArtistDeduplicationUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.utils.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class LastfmArtistSearchResponseProcessor extends LastfmApiResponseProcessor<ArtistSearchDtoRoot> {

    private final LastfmApiDtoProcessingOrchestrator dtoProcessingService;
    private final LastfmArtistService artistService;
    private final EntityFactory<LastfmArtist, ArtistSearchArtistDto> artistFactory;

    @Value("${lastfm.tasks.response-parse.methods.artist.search.artist-similarity-threshold}")
    private double artistSimilarityThreshold;

    private static final List<EntityAttributeHandler<LastfmArtist, ?, ArtistSearchArtistDto>> artistAttrHandlers;
    static {
        EntityAttributeHandlerFactory<LastfmArtist, ArtistSearchArtistDto> factory =
            new EntityAttributeHandlerFactory<>(LastfmArtist.class, ArtistSearchArtistDto.class);
        artistAttrHandlers = List.of(
            factory.createHandler(LastfmAttribute.MBID,  false, "mbid"),
            factory.createHandler(LastfmAttribute.URL, false, "url"),
            factory.createHandler(LastfmAttribute.LISTENERS_COUNT, false, "listenersCount")
        );
    }

    protected LastfmArtistSearchResponseProcessor(
        LastfmApiDtoProcessingOrchestrator dtoProcessingService, LastfmArtistService artistService,
        EntityFactory<LastfmArtist, ArtistSearchArtistDto> artistFactory,
        @Qualifier(MappingConfig.LASTFM_API_RESPONSE_OBJECT_MAPPER_BEAN_NAME) ObjectMapper objectMapper
    ) {
        super(ArtistSearchDtoRoot.class, objectMapper);

        this.dtoProcessingService = dtoProcessingService;
        this.artistService = artistService;
        this.artistFactory = artistFactory;
    }

    @Override
    public ApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_SEARCH;
    }

    @Override
    protected void processResponse(LastfmApiResponse sourceApiResponse) throws IOException {
        ArtistSearchDtoRoot dtoRoot = parseResponse(sourceApiResponse);

        updateArtists(dtoRoot, sourceApiResponse);
    }

    private void updateArtists(ArtistSearchDtoRoot dtoRoot, LastfmApiResponse sourceApiResponse) {
        LastfmApiCall sourceApiCall = sourceApiResponse.getApiCall();
        String searchString = sourceApiCall.getParams().get(LastfmApiConstants.PARAM_NAME_ARTIST);
        List<ArtistSearchArtistDto> dtos = filterArtistsForSaving(dtoRoot.getRootObject().getMatches().getArtists(), searchString);

        var result = dtoProcessingService.process(
            sourceApiCall,
            dtos,
            artistFactory,
            artistAttrHandlers,
            artistService
        );

        log.info("Saved {} found artists", result.actualEntities().size());
        log.info("Saved {} found artists' attributes", result.savedAttributeRecordsCount());

        // set statuses of pending artists to pre-approved
        List<LastfmArtist> artistsToPreApprove = result.actualEntities().stream()
                .filter(a -> a.getApprovalStatus() == ApprovalStatus.PENDING)
                .toList();
        if (!artistsToPreApprove.isEmpty()) {
            artistsToPreApprove.forEach(a -> a.setApprovalStatus(ApprovalStatus.PRE_APPROVED));
            artistService.saveAll(artistsToPreApprove);
        }
    }

    private List<ArtistSearchArtistDto> filterArtistsForSaving(List<ArtistSearchArtistDto> artistDtos, String searchString) {
        List<ArtistSearchArtistDto> result = new ArrayList<>();
        for (ArtistSearchArtistDto dto : artistDtos) {
            if (StringUtils.getSimilarity(dto.getName(), searchString) > artistSimilarityThreshold) {
                result.add(dto);
            }
        }

        result = ArtistDeduplicationUtils.deduplicateArtistDtos(result);

        return result;
    }
}
