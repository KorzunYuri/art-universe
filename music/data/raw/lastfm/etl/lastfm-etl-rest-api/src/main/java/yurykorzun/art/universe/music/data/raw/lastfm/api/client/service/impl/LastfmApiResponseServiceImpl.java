package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;

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
    public LastfmApiResponseDto getApiResponseById(long id) {
        return repository.findById(id)
            .map(LastfmApiResponseDto::from)
            .orElseThrow(() -> new CustomEntityNotFoundException("response", id));
    }

    @Override
    public JsonNode getApiResponseBody(Long id) {
        return repository.findById(id)
            .map(LastfmApiResponse::getResponseBody)
            .map(this::responseBodyToJson)
            .orElseThrow(() -> new CustomEntityNotFoundException("response", id));
    }

    private JsonNode responseBodyToJson(String responseBody) {
        try {
            return objectMapper.readTree(responseBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
