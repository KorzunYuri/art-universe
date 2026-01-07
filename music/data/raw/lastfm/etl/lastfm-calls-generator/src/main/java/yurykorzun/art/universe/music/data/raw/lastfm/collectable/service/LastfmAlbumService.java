package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.List;

public interface LastfmAlbumService{
    List<LastfmAlbum> findAlbumsForGetInfo();
}
