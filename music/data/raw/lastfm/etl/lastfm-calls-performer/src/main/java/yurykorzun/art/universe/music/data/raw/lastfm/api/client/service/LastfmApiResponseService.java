package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;

public interface LastfmApiResponseService {

    long createResponse(LastfmApiResponseCreateRequest dto);

}
