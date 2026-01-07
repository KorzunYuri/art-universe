package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.util.List;

public interface LastfmApiCallService {

    List<Long> createApiCalls(List<LastfmApiCallCreateRequest> lastfmApiCallCreateRequests);

    List<LastfmApiCall> findAllUnexpiredByType(LastfmApiCallType apiCallType);
}
