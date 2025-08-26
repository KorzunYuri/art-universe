package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;

public interface LastfmApiResponseService {

    long createResponse(LastfmApiResponseCreateRequest dto);

    void processResponses();

    LastfmApiResponseDto getApiResponseById(long id);

    JsonNode getApiResponseBody(Long id);
}
