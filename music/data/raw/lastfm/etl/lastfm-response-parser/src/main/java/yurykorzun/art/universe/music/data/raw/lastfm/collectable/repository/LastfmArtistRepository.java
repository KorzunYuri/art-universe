package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

import java.util.Collection;
import java.util.List;

public interface LastfmArtistRepository extends BaseLastfmArtistRepository {

    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

}
