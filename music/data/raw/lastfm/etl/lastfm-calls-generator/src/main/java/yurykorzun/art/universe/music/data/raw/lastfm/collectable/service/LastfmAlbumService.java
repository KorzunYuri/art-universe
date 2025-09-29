package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.List;

public interface LastfmAlbumService{

    /**
     * Find albums for album.getInfo API call with the following priority:
     * 1. Albums with missing playCount and listenersCount
     * 2. Prioritize albums from popular artists (join by artist_id, sort by listenersCount)
     * 
     * @return List of albums to process with album.getInfo API call
     */
    List<LastfmAlbum> findAlbumsForGetInfo();
}
