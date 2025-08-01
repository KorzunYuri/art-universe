package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;

import java.util.List;
import java.util.Optional;

public interface LastfmAlbumService extends EntityService<LastfmAlbum> {

    List<LastfmAlbum> findAllByUrls(List<String> urls);

    Optional<LastfmAlbum> findById(Long entityId);
}
