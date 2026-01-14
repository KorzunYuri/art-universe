package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.BaseLastfmArtistRepository;

import java.util.Collection;
import java.util.List;

public interface LastfmArtistRepository extends BaseLastfmArtistRepository {

    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

}
