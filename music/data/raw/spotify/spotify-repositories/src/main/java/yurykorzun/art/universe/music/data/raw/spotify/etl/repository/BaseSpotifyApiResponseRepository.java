package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;

@NoRepositoryBean
public interface BaseSpotifyApiResponseRepository extends JpaRepository<SpotifyApiResponse, Long> {
}
