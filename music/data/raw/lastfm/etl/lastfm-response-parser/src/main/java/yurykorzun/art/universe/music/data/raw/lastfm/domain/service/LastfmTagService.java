package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

import java.util.Optional;

public interface LastfmTagService extends EntityService<LastfmTag> {

    Optional<LastfmTag> findById(Long id);

}
