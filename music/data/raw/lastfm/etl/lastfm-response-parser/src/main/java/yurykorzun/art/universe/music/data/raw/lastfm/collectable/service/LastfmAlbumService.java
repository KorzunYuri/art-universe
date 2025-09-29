package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.Optional;

public interface LastfmAlbumService extends EntityService<LastfmAlbum> {

    Optional<LastfmAlbum> findById(Long entityId);

}
