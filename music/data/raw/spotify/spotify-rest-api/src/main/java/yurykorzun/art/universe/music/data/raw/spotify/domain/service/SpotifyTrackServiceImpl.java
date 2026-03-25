package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyArtistRepository;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyTrackRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SpotifyTrackServiceImpl implements SpotifyTrackService {

    private final SpotifyTrackRepository trackRepository;
    private final SpotifyArtistRepository artistRepository;

    @Override
    public SpotifyTrackResponseDto findById(Long id) {
        SpotifyTrack track = trackRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Track not found with id: " + id));
        String artistName = resolveArtistName(track.getPrimaryArtistId());
        return SpotifyTrackResponseDto.from(track, artistName);
    }

    @Override
    public Page<SpotifyTrackResponseDto> findAll(String search, Pageable pageable) {
        Page<SpotifyTrack> tracks = trackRepository.findTracks(search, pageable);
        Map<Long, String> artistNames = resolveArtistNames(
                tracks.getContent().stream()
                        .map(SpotifyTrack::getPrimaryArtistId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .toList()
        );
        return tracks.map(track -> SpotifyTrackResponseDto.from(
                track, track.getPrimaryArtistId() != null ? artistNames.get(track.getPrimaryArtistId()) : null
        ));
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
