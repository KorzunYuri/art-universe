package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;

public interface LastfmArtistSearchRequestService {

    LastfmArtistSearchRequest saveRequest(String searchString);

}
