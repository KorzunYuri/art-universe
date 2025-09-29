package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;

import java.util.List;

public interface LastfmArtistSearchRequestService {

    List<LastfmArtistSearchRequest> findUnprocessed(int batchLimit);

    List<LastfmArtistSearchRequest> saveRequests(List<LastfmArtistSearchRequest> searchRequests);
}
