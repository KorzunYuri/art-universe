package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;
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
    private final ObjectMapper objectMapper;
    private final LastfmApiResponseServiceImpl self;

    public LastfmApiResponseServiceImpl(
        LastfmApiResponseRepository repository,
        ObjectMapper objectMapper,
        @Lazy LastfmApiResponseServiceImpl self
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.self = self;
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

    @Override
    public LastfmApiResponseDto getApiResponseById(long id) {
        return repository.findById(id)
            .map(LastfmApiResponseDto::from)
            .orElseThrow(() -> new EntityNotFoundException("response", id));
    }

    @Override
    public JsonNode getApiResponseBody(Long id) {
        return repository.findById(id)
            .map(LastfmApiResponse::getResponseBody)
            .map(this::responseBodyToJson)
            .orElseThrow(() -> new EntityNotFoundException("response", id));
    }

    private JsonNode responseBodyToJson(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static LastfmApiResponse dtoToApiResponse(LastfmApiResponseCreateRequest dto) {
        return LastfmApiResponse.builder()
                .apiCall(dto.getApiCall())
                .responseBody(dto.getResponseBody())
            .build();
    }

}
