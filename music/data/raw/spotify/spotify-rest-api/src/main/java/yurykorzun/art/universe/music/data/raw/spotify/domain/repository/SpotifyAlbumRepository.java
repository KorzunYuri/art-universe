package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;

import java.util.Optional;

public interface SpotifyAlbumRepository extends BaseSpotifyAlbumRepository {

    Optional<SpotifyAlbum> findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);

    @Query("SELECT a FROM album a WHERE " +
           "(:search IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<SpotifyAlbum> findAlbums(String search, Pageable pageable);
}
