package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;

public interface LastfmApiResponseService {

    long createResponse(LastfmApiResponseCreateRequest dto);

    void setStatus(long id, ApiResponseStatus status) throws IllegalStateException;

    void processResponses();
}
