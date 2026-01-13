package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;

import java.util.List;

public interface LastfmApiCallService {

    List<Long> createApiCalls(List<LastfmApiCallCreateRequest> lastfmApiCallCreateRequests);

    List<LastfmApiCall> findAllUnexpiredByType(LastfmApiCallType apiCallType);
}
