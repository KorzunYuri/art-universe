package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;

import java.util.Optional;

public interface SpotifyArtistRepository extends BaseSpotifyArtistRepository {

    Optional<SpotifyArtist> findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);

    @Query("SELECT a FROM artist a WHERE " +
           "(:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SpotifyArtist> findArtists(String search, Pageable pageable);
}
