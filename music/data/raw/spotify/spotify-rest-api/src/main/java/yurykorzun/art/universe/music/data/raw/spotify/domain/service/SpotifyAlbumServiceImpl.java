package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyAlbumRepository;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyArtistRepository;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyTrackRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyAlbumServiceImpl implements SpotifyAlbumService {

    private final SpotifyAlbumRepository albumRepository;
    private final SpotifyTrackRepository trackRepository;
    private final SpotifyArtistRepository artistRepository;

    @Override
    public SpotifyAlbumResponseDto findById(Long id) {
        SpotifyAlbum album = albumRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Album not found with id: " + id));
        String artistName = resolveArtistName(album.getPrimaryArtistId());
        return SpotifyAlbumResponseDto.from(album, artistName);
    }

    @Override
    public Page<SpotifyAlbumResponseDto> findAll(String search, Pageable pageable) {
        Page<SpotifyAlbum> albums = albumRepository.findAlbums(search, pageable);
        Map<Long, String> artistNames = resolveArtistNames(
                albums.getContent().stream()
                        .map(SpotifyAlbum::getPrimaryArtistId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );
        return albums.map(album -> SpotifyAlbumResponseDto.from(
                album, album.getPrimaryArtistId() != null ? artistNames.get(album.getPrimaryArtistId()) : null
        ));
    }

    @Override
    public List<SpotifyAlbumTrackResponseDto> findAlbumTracks(Long albumId) {
        return trackRepository.findByAlbumId(albumId).stream()
                .map(SpotifyAlbumTrackResponseDto::from)
                .toList();
    }

    private String resolveArtistName(Long artistId) {
        if (artistId == null) return null;
        return artistRepository.findById(artistId)
                .map(SpotifyArtist::getName)
                .orElse(null);
    }

    private Map<Long, String> resolveArtistNames(List<Long> artistIds) {
        if (artistIds.isEmpty()) return Map.of();
        return artistRepository.findAllById(artistIds).stream()
                .collect(Collectors.toMap(SpotifyArtist::getId, SpotifyArtist::getName, (a, b) -> a));
    }
}
