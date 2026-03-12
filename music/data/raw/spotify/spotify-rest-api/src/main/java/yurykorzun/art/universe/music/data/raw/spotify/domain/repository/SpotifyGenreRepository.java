package yurykorzun.art.universe.music.data.raw.spotify.domain.repository;

import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyGenre;

import java.util.Optional;

public interface SpotifyGenreRepository extends BaseSpotifyGenreRepository {

    Optional<SpotifyGenre> findBySpotifyId(String spotifyId);

    boolean existsBySpotifyId(String spotifyId);
}
