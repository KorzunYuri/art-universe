package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

import java.util.Collection;
import java.util.List;

public interface BaseLastfmTagRepository extends JpaRepository<LastfmTag, Long> {

    List<LastfmTag> findAllByNameIn(Collection<String> names);

}
