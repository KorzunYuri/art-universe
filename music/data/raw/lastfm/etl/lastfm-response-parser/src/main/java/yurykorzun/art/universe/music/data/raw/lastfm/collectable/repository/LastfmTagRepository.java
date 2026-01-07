package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

import java.util.Collection;
import java.util.List;

public interface LastfmTagRepository extends BaseLastfmTagRepository {

    List<LastfmTag> findAllByNameIn(Collection<String> names);

}
