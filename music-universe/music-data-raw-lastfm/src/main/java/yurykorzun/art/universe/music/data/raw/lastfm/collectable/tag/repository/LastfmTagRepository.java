package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.Collection;
import java.util.List;

@Repository
public interface LastfmTagRepository extends JpaRepository<LastfmTag, Long> {

    List<LastfmTag> findAllByNameIn(Collection<String> names);

}
