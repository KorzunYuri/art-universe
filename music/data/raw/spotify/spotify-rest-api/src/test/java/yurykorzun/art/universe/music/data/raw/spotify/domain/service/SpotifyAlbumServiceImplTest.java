package yurykorzun.art.universe.music.data.raw.spotify.domain.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyAlbumRepository;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyArtistRepository;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyTrackRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyAlbumServiceImplTest {

    @Mock
    private SpotifyAlbumRepository albumRepository;

    @Mock
    private SpotifyTrackRepository trackRepository;

    @Mock
    private SpotifyArtistRepository artistRepository;

    @InjectMocks
    private SpotifyAlbumServiceImpl service;

    private SpotifyAlbum buildAlbumMock(Long artistId) {
        SpotifyAlbum album = mock(SpotifyAlbum.class);
        when(album.getId()).thenReturn(1L);
        when(album.getSpotifyId()).thenReturn("album-spotify-id");
        when(album.getName()).thenReturn("Certified Lover Boy");
        when(album.getTotalTracks()).thenReturn(21);
        when(album.getPrimaryArtistId()).thenReturn(artistId);
        when(album.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
        return album;
    }

    private SpotifyArtist buildArtistMock(String name) {
        SpotifyArtist artist = mock(SpotifyArtist.class);
        when(artist.getName()).thenReturn(name);
        return artist;
    }

    private SpotifyArtist buildArtistMockWithId(Long id, String name) {
        SpotifyArtist artist = buildArtistMock(name);
        when(artist.getId()).thenReturn(id);
        return artist;
    }

    @Test
    void findById_shouldReturnDtoWithArtistName_whenArtistExists() {
        SpotifyAlbum album = buildAlbumMock(10L);
        SpotifyArtist artist = buildArtistMock("Drake");
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));
        when(artistRepository.findById(10L)).thenReturn(Optional.of(artist));

        SpotifyAlbumResponseDto dto = service.findById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Certified Lover Boy");
        assertThat(dto.primaryArtistName()).isEqualTo("Drake");
    }

    @Test
    void findById_shouldReturnDtoWithNullArtistName_whenNoArtist() {
        SpotifyAlbum album = buildAlbumMock(null);
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        SpotifyAlbumResponseDto dto = service.findById(1L);

        assertThat(dto.primaryArtistName()).isNull();
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    void findAll_shouldReturnMappedPageWithArtistNames() {
        SpotifyAlbum album = buildAlbumMock(10L);
        SpotifyArtist artist = buildArtistMockWithId(10L, "Drake");
        Page<SpotifyAlbum> repoPage = new PageImpl<>(List.of(album), PageRequest.of(0, 20), 1);
        when(albumRepository.findAlbums(isNull(), any())).thenReturn(repoPage);
        when(artistRepository.findAllById(List.of(10L))).thenReturn(List.of(artist));

        Page<SpotifyAlbumResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Certified Lover Boy");
        assertThat(result.getContent().get(0).primaryArtistName()).isEqualTo("Drake");
    }

    @Test
    void findAll_shouldHandleAlbumsWithoutArtistId() {
        SpotifyAlbum album = buildAlbumMock(null);
        Page<SpotifyAlbum> repoPage = new PageImpl<>(List.of(album), PageRequest.of(0, 20), 1);
        when(albumRepository.findAlbums(isNull(), any())).thenReturn(repoPage);

        Page<SpotifyAlbumResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).primaryArtistName()).isNull();
    }

    @Test
    void findAlbumTracks_shouldReturnMappedTracks() {
        SpotifyTrack track = mock(SpotifyTrack.class);
        when(track.getId()).thenReturn(100L);
        when(track.getName()).thenReturn("Champagne Poetry");
        when(track.getSpotifyId()).thenReturn("track-spotify-id");
        when(track.getTrackNumber()).thenReturn(1);
        when(track.getDurationMs()).thenReturn(287000);
        when(trackRepository.findByAlbumId(1L)).thenReturn(List.of(track));

        List<SpotifyAlbumTrackResponseDto> result = service.findAlbumTracks(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).trackName()).isEqualTo("Champagne Poetry");
        assertThat(result.get(0).trackNumber()).isEqualTo(1);
    }

    @Test
    void findAlbumTracks_shouldReturnEmptyList_whenNoTracks() {
        when(trackRepository.findByAlbumId(1L)).thenReturn(List.of());

        List<SpotifyAlbumTrackResponseDto> result = service.findAlbumTracks(1L);

        assertThat(result).isEmpty();
    }
}
