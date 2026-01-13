package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

import java.util.Collection;

@Service
@Slf4j
public class LastfmApiCallServiceImpl implements LastfmApiCallService {

    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmApiResponseService responseService;
    private final LastfmApiClient apiClient;
    private final LastfmApiCallServiceImpl self;

    private final RateLimiter rateLimiter;

    public LastfmApiCallServiceImpl(
            LastfmApiCallRepository apiCallRepository,
            LastfmApiResponseService responseService,
            LastfmApiClient apiClient,
            @Lazy LastfmApiCallServiceImpl self,
            @Value("${lastfm.tasks.calls-perform.calls-per-sec}") double apiClientCallsPerSec
    ) {
        this.apiCallRepository = apiCallRepository;
        this.responseService = responseService;
        this.apiClient = apiClient;
        this.self = self;

        this.rateLimiter = RateLimiter.create(apiClientCallsPerSec);
    }

    @Override
    public void executeApiCalls() {
        //   TODO design complex priority logic to fit LastFm API calls rate limit
        Collection<LastfmApiCall> apiCalls = apiCallRepository.findAllUnprocessedUnexpired();
        apiCalls.forEach(apiCall -> {
            log.info("initiating API call {} of type {} for entity {}: {}",
                apiCall.getId(),
                apiCall.getType(),
                apiCall.getEntityType(),
                apiCall.getEntityId()
            );
            rateLimiter.acquire();
            try {
                self.makeApiCall(apiCall);
                log.info("API call has been performed");
            } catch (Exception ex) {
                log.error("Failed to process API call {}: {}", apiCall.getId(), ex.getMessage(), ex);
            }
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void makeApiCall(LastfmApiCall call) {
        self.updateApiCallStatus(call, ApiCallStatus.PROCESSING);

        try {
            String response = self.makeApiCallWithRetry(call);
            responseService.createResponse(createApiResponseCreateDto(call, response));
            self.updateApiCallStatus(call, ApiCallStatus.SUCCESSFUL);
        } catch (Exception ex) {
            self.updateApiCallStatus(call, ApiCallStatus.FAILED);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void updateApiCallStatus(LastfmApiCall call, ApiCallStatus status) {
        call.setStatus(status);
        apiCallRepository.save(call);
    }

    private LastfmApiResponseCreateRequest createApiResponseCreateDto(LastfmApiCall call, String response) {
        return LastfmApiResponseCreateRequest.builder()
                .apiCall(call)
                .responseBody(response)
            .build();
    }

    @Retryable(
            retryFor = {Exception.class},
            maxAttemptsExpression = "${lastfm.tasks.calls-perform.retry.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${lastfm.tasks.calls-perform.retry.initial-delay-ms}",
                    multiplierExpression = "${lastfm.tasks.calls-perform.retry.multiplier}"
            )
    )
    protected String makeApiCallWithRetry(LastfmApiCall call) {
        return apiClient.makeApiCall(call);
    }
}
