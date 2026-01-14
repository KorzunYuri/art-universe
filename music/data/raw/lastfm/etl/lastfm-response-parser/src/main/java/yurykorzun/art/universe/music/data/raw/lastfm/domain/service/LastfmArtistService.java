package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

import java.util.Optional;

public interface LastfmArtistService extends EntityService<LastfmArtist> {

    Optional<LastfmArtist> findById(Long id);

}
