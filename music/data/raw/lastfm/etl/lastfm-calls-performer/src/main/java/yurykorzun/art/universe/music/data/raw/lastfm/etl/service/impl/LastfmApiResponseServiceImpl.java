package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;

@Slf4j
@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;
    private final ObjectMapper objectMapper;

    public LastfmApiResponseServiceImpl(
        LastfmApiResponseRepository repository,
        ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
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

    private static LastfmApiResponse dtoToApiResponse(LastfmApiResponseCreateRequest dto) {
        return LastfmApiResponse.builder()
                .apiCall(dto.getApiCall())
                .responseBody(dto.getResponseBody())
            .build();
    }

}
