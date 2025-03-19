package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.List;

public interface LastfmApiCallService {

    long createApiCall(LastfmApiCallCreateRequest dto);

    void setStatus(long id, ApiCallStatus status) throws IllegalStateException;

    void triggerApiCalls();

    void createApiCalls(List<LastfmApiCallCreateRequest> lastfmApiCallCreateRequests);

    void expireApiCallsForType(LastfmApiCallType type);

}
