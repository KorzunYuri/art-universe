package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

@NoRepositoryBean
public interface BaseLastfmAlbumRepository extends JpaRepository<LastfmAlbum, Long> {
}
