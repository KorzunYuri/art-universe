package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.BaseLastfmTagRepository;

import java.util.Collection;
import java.util.List;

public interface LastfmTagRepository extends BaseLastfmTagRepository {

    List<LastfmTag> findAllByNameIn(Collection<String> names);

}
