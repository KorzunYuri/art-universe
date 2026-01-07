package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;

@NoRepositoryBean
public interface BaseLastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {
}
