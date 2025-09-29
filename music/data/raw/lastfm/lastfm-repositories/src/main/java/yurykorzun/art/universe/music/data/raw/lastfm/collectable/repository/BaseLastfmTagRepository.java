package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

import java.util.Collection;
import java.util.List;

@NoRepositoryBean
public interface BaseLastfmTagRepository extends JpaRepository<LastfmTag, Long> {
}
