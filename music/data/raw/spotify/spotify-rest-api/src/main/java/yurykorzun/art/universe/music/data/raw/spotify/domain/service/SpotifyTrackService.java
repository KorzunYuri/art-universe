package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;

public interface SpotifyTrackService {
    SpotifyTrackResponseDto findById(Long id);
    Page<SpotifyTrackResponseDto> findAll(String search, Pageable pageable);
}
