package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;

public interface LastfmApiResponseService {

    long createResponse(LastfmApiResponseCreateRequest dto);

    void setStatus(long id, ApiResponseStatus status) throws IllegalStateException;

    void processResponses();

    LastfmApiResponseDto getApiResponseById(long id);

    JsonNode getApiResponseBody(Long id);
}
