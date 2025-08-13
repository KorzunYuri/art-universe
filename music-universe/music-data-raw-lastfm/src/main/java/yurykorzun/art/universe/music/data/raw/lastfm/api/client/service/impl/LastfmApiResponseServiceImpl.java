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
    public void setStatus(long id, ApiResponseStatus status) throws IllegalStateException {
        LastfmApiResponse response = repository.getReferenceById(id);
        response.setStatus(status);
        repository.save(response);
    }

    @Override
    public void processResponses() {
        List<LastfmApiResponse> unprocessed = repository.findAllPending();
        log.info("unprocessed API responses left: {}", unprocessed.size());
        
        // Process each response in a separate transaction
        for (LastfmApiResponse response : unprocessed) {
            try {
                self.processResponse(response);
            } catch (Exception e) {
                // one error doesn't prevent the other responses from being parsed
                log.error("Failed to process response with ID {}: {}", response.getId(), e.getMessage(), e);
            }
        }
    }

    /**
     * Process each response in a separate transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processResponse(LastfmApiResponse response) {
        LastfmApiResponseProcessor<?> processor = LastfmApiResponseProcessorsRegistry.get(
                response.getApiCall().getType().getResponseDtoClass());
        
        if (processor == null) {
            log.warn("No processor found for response type: {}", response.getApiCall().getType());
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            response.setUpdatedAt(Instant.now());
            repository.save(response);
            return;
        }

        try {
            log.info("Start processing API response ID {} from method {}", 
                response.getId(), processor.getApiCallType().getMethod());
            
            response.setStatus(ApiResponseStatus.PROCESSING);
            repository.save(response);
            
            processor.process(response);
            
            response.setStatus(ApiResponseStatus.COMPLETED);
            log.info("Successfully processed API response ID {} from method {}", 
                response.getId(), processor.getApiCallType().getMethod());
                
        } catch (IOException e) {
            log.error("Processing error for API response ID {} from method {}: {}", 
                response.getId(), processor.getApiCallType().getMethod(), e.getMessage(), e);
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
        } catch (Exception e) {
            log.error("Unexpected error for API response ID {} from method {}: {}", 
                response.getId(), processor.getApiCallType().getMethod(), e.getMessage(), e);
            response.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            throw e; // propagate for transaction rollback
        } finally {
            response.setUpdatedAt(Instant.now());
            repository.save(response);
        }
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
