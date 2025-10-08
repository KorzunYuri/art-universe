package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import com.fasterxml.jackson.databind.JsonNode;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseDto;

public interface LastfmApiResponseService {

    LastfmApiResponseDto getApiResponseById(long id);

    JsonNode getApiResponseBody(Long id);
}
