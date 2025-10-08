package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

import java.util.Optional;

public interface LastfmTagService extends EntityService<LastfmTag> {

    Optional<LastfmTag> findById(Long id);

}
