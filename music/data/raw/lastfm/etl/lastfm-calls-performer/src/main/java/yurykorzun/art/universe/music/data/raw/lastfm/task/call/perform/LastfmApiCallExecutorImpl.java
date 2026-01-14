package yurykorzun.art.universe.music.data.raw.lastfm.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiClient;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

@Service
@Slf4j
public class LastfmApiCallExecutorImpl implements LastfmApiCallExecutor {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiResponseService apiResponseService;
    private final LastfmApiClient apiClient;
    private final LastfmApiCallExecutorImpl self;

    public LastfmApiCallExecutorImpl(
        LastfmApiCallService apiCallService,
        LastfmApiResponseService apiResponseService,
        LastfmApiClient apiClient,
        @Lazy LastfmApiCallExecutorImpl self
    ) {
        this.apiCallService = apiCallService;
        this.apiResponseService = apiResponseService;
        this.apiClient = apiClient;
        this.self = self;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void execute(LastfmApiCall call) {
        apiCallService.updateApiCallStatus(call, ApiCallStatus.PROCESSING);

        try {
            String response = self.makeApiCallWithRetry(call);
            apiResponseService.createResponse(createApiResponseCreateDto(call, response));
            apiCallService.updateApiCallStatus(call, ApiCallStatus.SUCCESSFUL);
        } catch (Exception ex) {
            apiCallService.updateApiCallStatus(call, ApiCallStatus.FAILED);
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
