package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

import java.util.Optional;

public interface TestLastfmArtistRepository extends BaseLastfmArtistRepository {

    Optional<LastfmArtist> findByName(String name);

}
