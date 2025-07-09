package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

import java.util.List;

public interface LastfmAlbumService extends EntityService<LastfmAlbum> {

    List<LastfmAlbum> findAllByUrls(List<String> urls);

}
