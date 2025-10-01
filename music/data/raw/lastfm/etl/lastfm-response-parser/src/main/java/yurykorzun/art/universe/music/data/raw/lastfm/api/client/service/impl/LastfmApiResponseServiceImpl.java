package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessorsRegistry;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;
    private final LastfmApiResponseServiceImpl self;

    public LastfmApiResponseServiceImpl(
        LastfmApiResponseRepository repository,
        @Lazy LastfmApiResponseServiceImpl self
    ) {
        this.repository = repository;
        this.self = self;
    }

    @Override
    public void processResponses() {
        List<LastfmApiResponse> unprocessed = repository.findAllPending();
        log.info("Unprocessed API responses left: {}", unprocessed.size());

        for (LastfmApiResponse response : unprocessed) {
            self.processResponse(response);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processResponse(LastfmApiResponse response) {
        try {
            self.setStatus(response, ApiResponseStatus.PROCESSING);

            LastfmApiResponseProcessor<?> processor =
                    LastfmApiResponseProcessorsRegistry.get(response.getApiCall().getType().getResponseDtoClass());

            if (processor == null) {
                log.warn("No processor found for response type: {}", response.getApiCall().getType());
                self.setStatus(response, ApiResponseStatus.PROCESSING_ERROR);
                return;
            }

            log.info("Start processing API response ID {} from method {}",
                    response.getId(), processor.getApiCallType().getMethod());

            processor.process(response);

            self.setStatus(response, ApiResponseStatus.COMPLETED);

            log.info("Successfully processed API response ID {} from method {}",
                    response.getId(), processor.getApiCallType().getMethod());

        } catch (Exception e) {
            log.error("Error processing API response ID {}: {}", response.getId(), e.getMessage(), e);
            self.setStatus(response, ApiResponseStatus.PROCESSING_ERROR);
        }
    }

    /**
     * Update response status in a separate transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setStatus(LastfmApiResponse response, ApiResponseStatus status) {
        response.setStatus(status);
        response.setUpdatedAt(Instant.now());
        repository.save(response);
    }
}
