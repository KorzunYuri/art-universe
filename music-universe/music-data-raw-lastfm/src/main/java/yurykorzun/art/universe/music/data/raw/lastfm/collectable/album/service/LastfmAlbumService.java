package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

import java.util.List;

public interface LastfmAlbumService {

    List<LastfmAlbum> findAllByUrls(List<String> urls);

    List<LastfmAlbum> saveAlbums(List<LastfmAlbum> lastfmAlbums);

}
