package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service;

import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallCreateRequest;

public interface LastfmApiCallService {

    long createRequest(LastfmApiCallCreateRequest dto);

    void setStatus(long id, ApiCallStatus status) throws IllegalStateException;
}
