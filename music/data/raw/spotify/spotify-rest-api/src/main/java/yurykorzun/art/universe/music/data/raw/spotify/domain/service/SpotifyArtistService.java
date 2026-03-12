package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;

public interface SpotifyArtistService {

    SpotifyArtistResponseDto findById(Long id);

    SpotifyArtistResponseDto findBySpotifyId(String spotifyId);

    Page<SpotifyArtistResponseDto> findAll(String search, Pageable pageable);
}
