package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallPrioritizer;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class LastfmApiCallServiceImpl implements LastfmApiCallService {

    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmApiResponseService responseService;
    private final LastfmApiCallPrioritizer apiCallPrioritizer;
    private final LastfmApiClient apiClient;
    private final LastfmApiCallServiceImpl self;

    private final RateLimiter rateLimiter;

    public LastfmApiCallServiceImpl(
            LastfmApiCallRepository apiCallRepository,
            LastfmApiResponseService responseService,
            LastfmApiCallPrioritizer apiCallPrioritizer,
            LastfmApiClient apiClient,
            @Lazy LastfmApiCallServiceImpl self,
            @Value("${lastfm.client.callsPerSec}") double apiClientCallsPerSec
    ) {
        this.apiCallRepository = apiCallRepository;
        this.responseService = responseService;
        this.apiCallPrioritizer = apiCallPrioritizer;
        this.apiClient = apiClient;
        this.self = self;

        this.rateLimiter = RateLimiter.create(apiClientCallsPerSec);
    }

    @Override
    @Transactional
    public long createApiCall(LastfmApiCallCreateRequest dto) {
        LastfmApiCall call = dtoToApiCall(dto);
        call.setStatus(ApiCallStatus.PENDING);
        LastfmApiCall lastfmApiCall = apiCallRepository.save(call);

        return lastfmApiCall.getId();
    }

    @Override
    @Transactional
    public List<Long> createApiCalls(List<LastfmApiCallCreateRequest> lastfmApiCallCreateRequests) {
        List<LastfmApiCall> calls = lastfmApiCallCreateRequests.stream()
                .map(this::dtoToApiCall)
                .peek(t -> t.setStatus(ApiCallStatus.PENDING))
            .toList();

        List<LastfmApiCall> apiCalls = apiCallRepository.saveAll(calls);

        return apiCalls.stream().map(LastfmApiCall::getId).toList();
    }

    @Override
    @Transactional
    public void expireApiCallsForType(LastfmApiCallType type) {
        apiCallRepository.expireOutdatedApiCallsByType(type);
    }

    @Override
    public List<LastfmApiCall> findAllUnexpiredByType(LastfmApiCallType apiCallType) {
        return apiCallRepository.findAllUnexpiredByType(apiCallType);
    }

    @Override
    @Transactional
    public void setStatus(long id, ApiCallStatus status) throws IllegalStateException {
        LastfmApiCall call = apiCallRepository.getReferenceById(id);
        call.setStatus(status);
        apiCallRepository.save(call);
    }

    private LastfmApiCall dtoToApiCall(LastfmApiCallCreateRequest dto) {
        return LastfmApiCall.builder()
                .type(dto.getType())
                .dataSnapshotId(dto.getDataSnapshotId())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .dueDttm(dto.getDueDttm())
                .params(dto.getParams())
            .build();
    }

    @Override
    public void triggerApiCalls() {
        //   TODO design complex priority logic to fit LastFm API calls rate limit
        Collection<LastfmApiCall> apiCalls = apiCallRepository.findAllUnprocessedUnexpired();
        apiCalls = apiCallPrioritizer.prioritizeApiCalls(apiCalls);
        apiCalls.forEach(apiCall -> {
            log.info("initiating API call of type {} for entity {}", apiCall.getType(), apiCall.getEntityId());
            rateLimiter.acquire();
            self.makeApiCall(apiCall);
            log.info("API call has been performed");
        });
    }

    @Transactional
    protected void makeApiCall(LastfmApiCall call) {
        call.setStatus(ApiCallStatus.PROCESSING);
        apiCallRepository.save(call);

        try {
            String response = makeApiCallWithRetry(call);
            responseService.createResponse(createApiResponseCreateDto(call, response));
            call.setStatus(ApiCallStatus.SUCCESSFUL);
        } catch (Exception ex) {
            call.setStatus(ApiCallStatus.FAILED);
        } finally {
            apiCallRepository.save(call);
        }
    }

    private LastfmApiResponseCreateRequest createApiResponseCreateDto(LastfmApiCall call, String response) {
        return LastfmApiResponseCreateRequest.builder()
                .apiCall(call)
                .responseBody(response)
            .build();
    }

    @Retryable(
            retryFor = {Exception.class},
            maxAttemptsExpression = "${lastfm.client.retry.max-attempts}",
            backoff = @Backoff(
                    delayExpression = "${lastfm.client.retry.initial-delay-ms}",
                    multiplierExpression = "${lastfm.client.retry.multiplier}"
            )
    )
    protected String makeApiCallWithRetry(LastfmApiCall call) {
        return apiClient.makeApiCall(call);
    }
}
