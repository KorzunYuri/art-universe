package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

import java.util.Optional;

public interface LastfmAlbumService extends EntityService<LastfmAlbum> {

    Optional<LastfmAlbum> findById(Long entityId);

}
