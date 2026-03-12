package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

@NoRepositoryBean
public interface BaseSpotifyApiCallRepository extends JpaRepository<SpotifyApiCall, Long> {
}
