package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.spotify.staging.StagingIterationService;

import java.util.List;

@Service
@Slf4j
public class SpotifyApiResponseProcessingOrchestrator {

    private final SpotifyApiResponseRepository apiResponseRepository;
    private final StagingIterationService stagingIterationService;
    private final ConfigPropertyHolder configPropertyHolder;

    public SpotifyApiResponseProcessingOrchestrator(
        SpotifyApiResponseRepository apiResponseRepository,
        StagingIterationService stagingIterationService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.apiResponseRepository = apiResponseRepository;
        this.stagingIterationService = stagingIterationService;
        this.configPropertyHolder = configPropertyHolder;
    }

    public void processResponses() {
        List<SpotifyApiResponse> responses = apiResponseRepository.findAllPending();
        if (responses.isEmpty()) {
            stagingIterationService.sealIfExpired();
            return;
        }

        StagingIteration iteration = stagingIterationService.getOrCreateOpenIteration();
        int totalStaged = 0;

        for (SpotifyApiResponse response : responses) {
            int staged = processSingle(response, iteration);
            totalStaged += staged;
        }

        if (totalStaged > 0) {
            stagingIterationService.incrementRecordsStaged(iteration, totalStaged);
        }

        if (stagingIterationService.shouldSeal(iteration)) {
            stagingIterationService.seal(iteration);
        }

        log.info("Processed {} responses, staged {} records into iteration {}",
            responses.size(), totalStaged, iteration.getId());
    }

    @Transactional
    protected int processSingle(SpotifyApiResponse response, StagingIteration iteration) {
        SpotifyApiCallType callType = response.getApiCall().getType();

        if (!isParsingEnabled(callType)) {
            log.info("Parsing is disabled for call type {}, skipping response {}", callType, response.getId());
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            apiResponseRepository.save(response);
            return 0;
        }

        BaseSpotifyApiResponseProcessor processor = SpotifyApiResponseProcessorsRegistry.get(callType);

        if (processor == null) {
            log.warn("No processor registered for call type {}, skipping response {}", callType, response.getId());
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            apiResponseRepository.save(response);
            return 0;
        }

        response.setStatus(ApiResponseStatus.PROCESSING);
        apiResponseRepository.save(response);

        try {
            int staged = processor.process(response, iteration);
            response.setStatus(ApiResponseStatus.COMPLETED);
            apiResponseRepository.save(response);
            return staged;
        } catch (Exception e) {
            log.error("Failed to process response {} (type={}): {}", response.getId(), callType, e.getMessage(), e);
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            apiResponseRepository.save(response);
            return 0;
        }
    }

    private boolean isParsingEnabled(SpotifyApiCallType callType) {
        SpotifyParserProperty prop = SpotifyParserProperty.valueOf("PARSE_" + callType.name());
        return configPropertyHolder.getBoolean(prop);
    }
}
