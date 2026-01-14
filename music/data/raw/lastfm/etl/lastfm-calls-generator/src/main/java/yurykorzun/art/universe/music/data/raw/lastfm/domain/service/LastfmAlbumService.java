package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

import java.util.List;

public interface LastfmAlbumService{
    List<LastfmAlbum> findAlbumsForGetInfo();
}
