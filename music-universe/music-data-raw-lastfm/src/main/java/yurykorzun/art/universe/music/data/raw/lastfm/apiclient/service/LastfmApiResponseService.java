package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service;

import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiResponseCreateRequest;

public interface LastfmApiResponseService {

    long create(LastfmApiResponseCreateRequest dto);

    void setStatus(long id, ApiResponseStatus status) throws IllegalStateException;

}
