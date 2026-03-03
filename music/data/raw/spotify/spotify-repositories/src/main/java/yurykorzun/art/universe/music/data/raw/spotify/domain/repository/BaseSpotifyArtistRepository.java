package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;

@NoRepositoryBean
public interface BaseSpotifyArtistRepository extends JpaRepository<SpotifyArtist, Long> {
}
