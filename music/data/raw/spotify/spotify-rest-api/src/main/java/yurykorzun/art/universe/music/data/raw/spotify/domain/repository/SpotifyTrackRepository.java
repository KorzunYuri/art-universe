package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;

import java.util.Optional;

public interface SpotifyTrackRepository extends BaseSpotifyTrackRepository {

    Optional<SpotifyTrack> findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);

    @Query("SELECT t FROM track t WHERE " +
           "(:search IS NULL OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SpotifyTrack> findTracks(String search, Pageable pageable);
}
