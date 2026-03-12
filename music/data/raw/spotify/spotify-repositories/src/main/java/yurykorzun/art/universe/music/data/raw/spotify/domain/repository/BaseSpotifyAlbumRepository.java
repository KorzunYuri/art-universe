package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;

@NoRepositoryBean
public interface BaseSpotifyAlbumRepository extends JpaRepository<SpotifyAlbum, Long> {
}
