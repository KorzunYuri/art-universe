package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyArtistRepository;

@Service
public class SpotifyArtistServiceImpl implements SpotifyArtistService {

    private final SpotifyArtistRepository artistRepository;

    public SpotifyArtistServiceImpl(SpotifyArtistRepository artistRepository) {
        this.artistRepository = artistRepository;
    }

    @Override
    public SpotifyArtistResponseDto findById(Long id) {
        return artistRepository.findById(id)
                .map(SpotifyArtistResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with id: " + id));
    }

    @Override
    public SpotifyArtistResponseDto findBySpotifyId(String spotifyId) {
        return artistRepository.findBySpotifyId(spotifyId)
                .map(SpotifyArtistResponseDto::from)
                .orElseThrow(() -> new EntityNotFoundException("Artist not found with spotifyId: " + spotifyId));
    }

    @Override
    public Page<SpotifyArtistResponseDto> findAll(String search, Pageable pageable) {
        return artistRepository.findArtists(search, pageable)
                .map(SpotifyArtistResponseDto::from);
    }
}
