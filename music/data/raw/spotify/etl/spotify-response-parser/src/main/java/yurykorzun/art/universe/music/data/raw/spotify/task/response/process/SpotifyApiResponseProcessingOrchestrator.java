package yurykorzun.art.universe.music.data.raw.spotify.task.response.process;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
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

    public SpotifyApiResponseProcessingOrchestrator(
        SpotifyApiResponseRepository apiResponseRepository,
        StagingIterationService stagingIterationService
    ) {
        this.apiResponseRepository = apiResponseRepository;
        this.stagingIterationService = stagingIterationService;
    }

    public void processResponses() {
        List<SpotifyApiResponse> responses = apiResponseRepository.findAllPending();
        if (responses.isEmpty()) {
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
}
