package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessorsRegistry;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;
    private final LastfmApiResponseServiceImpl self;
    private final ObjectMapper objectMapper;

    public LastfmApiResponseServiceImpl(
        LastfmApiResponseRepository repository,
        @Lazy LastfmApiResponseServiceImpl self
    ) {
        this.repository = repository;
        this.self = self;

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public long createResponse(LastfmApiResponseCreateRequest dto) {
        LastfmApiResponse response = dtoToApiResponse(dto);
        response.setStatus(ApiResponseStatus.PENDING);
        try {
            validateResponseBody(response);
        } catch (JsonProcessingException e) {
            log.warn("Failed to validate response body");
            response.setStatus(ApiResponseStatus.VALIDATION_ERROR);
        }

        response = repository.save(response);
        return response.getId();
    }

    private void validateResponseBody(LastfmApiResponse response) throws JsonProcessingException {
        JsonNode actualObj = objectMapper.readTree(response.getResponseBody());
        if (actualObj.has("error")) {
            response.setStatus(ApiResponseStatus.IS_ERROR_RESPONSE);
            log.warn("API returned error: {}", actualObj.has("message") ? actualObj.get("message").asText() : "<unknown>");
        }
    }

    @Override
    public void setStatus(long id, ApiResponseStatus status) throws IllegalStateException {
        LastfmApiResponse response = repository.getReferenceById(id);
        response.setStatus(status);
        repository.save(response);
    }

    @Override
    public void triggerResponsesProcessing() {
        List<LastfmApiResponse> unprocessed = repository.findAllPending();
        log.info("unprocessed API responses left: {}", unprocessed.size());
        // TODO handle concurrent processing by several Processors, then process responses in parallel
        // TODO send responses to Processor not in parallel but in batches
        unprocessed.forEach(self::processResponse);
    }

    @Transactional
    protected void processResponse(LastfmApiResponse r) {
        LastfmApiResponseProcessor<?> processor = LastfmApiResponseProcessorsRegistry.get(
                r.getApiCall().getType().getResponseDtoClass());
        if (processor == null) {
            return;
        }

        try {
            log.info("start processing API response from method {}", processor.getApiCallType().getMethod());
            processor.process(r);
            r.setStatus(ApiResponseStatus.COMPLETED);
        } catch (IOException e) {
            r.setStatus(ApiResponseStatus.PROCESSING_ERROR);
        } finally {
            log.info("finished processing API response from method {}", processor.getApiCallType().getMethod());
        }
        r.setUpdatedAt(Instant.now());
        repository.save(r);
    }

    private static LastfmApiResponse dtoToApiResponse(LastfmApiResponseCreateRequest dto) {
        return LastfmApiResponse.builder()
                .apiCall(dto.getApiCall())
                .responseBody(dto.getResponseBody())
            .build();
    }

}
