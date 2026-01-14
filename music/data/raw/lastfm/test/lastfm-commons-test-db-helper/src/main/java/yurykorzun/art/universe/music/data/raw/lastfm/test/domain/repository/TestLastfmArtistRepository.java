package yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.BaseLastfmArtistRepository;

import java.util.Optional;

public interface TestLastfmArtistRepository extends BaseLastfmArtistRepository {

    Optional<LastfmArtist> findByName(String name);

}
