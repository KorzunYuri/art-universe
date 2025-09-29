package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

import java.util.Optional;

public interface LastfmArtistService extends EntityService<LastfmArtist> {

    Optional<LastfmArtist> findById(Long id);

}
