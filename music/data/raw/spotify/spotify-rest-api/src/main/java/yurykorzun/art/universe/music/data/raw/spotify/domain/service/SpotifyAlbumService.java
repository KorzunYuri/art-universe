package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumTrackResponseDto;

import java.util.List;

public interface SpotifyAlbumService {

    SpotifyAlbumResponseDto findById(Long id);

    Page<SpotifyAlbumResponseDto> findAll(String search, Pageable pageable);

    List<SpotifyAlbumTrackResponseDto> findAlbumTracks(Long albumId);
}
