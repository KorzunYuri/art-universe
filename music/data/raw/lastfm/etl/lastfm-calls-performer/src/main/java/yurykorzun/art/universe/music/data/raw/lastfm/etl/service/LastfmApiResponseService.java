package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiResponseCreateRequest;

public interface LastfmApiResponseService {

    long createResponse(LastfmApiResponseCreateRequest dto);

}
