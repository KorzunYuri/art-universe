package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;

public interface LastfmApiCallService {

    long create(LastfmApiCallCreateRequest dto);

    void setStatus(long id, ApiCallStatus status) throws IllegalStateException;

    void triggerApiCalls();
}
