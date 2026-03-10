package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;

import java.util.List;
import java.util.Optional;

@NoRepositoryBean
public interface BaseSpotifySearchAttemptRepository extends JpaRepository<SpotifySearchAttempt, Long> {

    List<SpotifySearchAttempt> findAllByStatus(SearchAttemptStatus status);

    Optional<SpotifySearchAttempt> findByApiCallId(Long apiCallId);
}
