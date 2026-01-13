package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;

import java.util.List;

public interface LastfmArtistSearchRequestService {

    List<LastfmArtistSearchRequest> findUnprocessed(int batchLimit);

    List<LastfmArtistSearchRequest> saveRequests(List<LastfmArtistSearchRequest> searchRequests);
}
