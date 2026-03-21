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
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyArtistRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistServiceImplTest {

    @Mock
    private SpotifyArtistRepository artistRepository;

    @InjectMocks
    private SpotifyArtistServiceImpl service;

    private SpotifyArtist buildArtistMock() {
        SpotifyArtist artist = mock(SpotifyArtist.class);
        when(artist.getId()).thenReturn(1L);
        when(artist.getSpotifyId()).thenReturn("3TVXtAsR1Inumwj472S9r4");
        when(artist.getName()).thenReturn("Drake");
        when(artist.getSpotifyUrl()).thenReturn("https://open.spotify.com/artist/3TVXtAsR1Inumwj472S9r4");
        when(artist.getUri()).thenReturn("spotify:artist:3TVXtAsR1Inumwj472S9r4");
        when(artist.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
        return artist;
    }

    @Test
    void findById_shouldReturnDto_whenFound() {
        SpotifyArtist artist = buildArtistMock();
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));

        SpotifyArtistResponseDto dto = service.findById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.spotifyId()).isEqualTo("3TVXtAsR1Inumwj472S9r4");
        assertThat(dto.name()).isEqualTo("Drake");
        assertThat(dto.approvalStatus()).isEqualTo(ApprovalStatus.PENDING.getCode());
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(artistRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    void findBySpotifyId_shouldReturnDto_whenFound() {
        SpotifyArtist artist = buildArtistMock();
        when(artistRepository.findBySpotifyId("3TVXtAsR1Inumwj472S9r4")).thenReturn(Optional.of(artist));

        SpotifyArtistResponseDto dto = service.findBySpotifyId("3TVXtAsR1Inumwj472S9r4");

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("Drake");
    }

    @Test
    void findBySpotifyId_shouldThrowEntityNotFoundException_whenNotFound() {
        when(artistRepository.findBySpotifyId("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findBySpotifyId("unknown"))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("unknown");
    }

    @Test
    void findAll_shouldReturnMappedPage() {
        SpotifyArtist artist = buildArtistMock();
        Page<SpotifyArtist> repoPage = new PageImpl<>(List.of(artist), PageRequest.of(0, 20), 1);
        when(artistRepository.findArtists(isNull(), any())).thenReturn(repoPage);

        Page<SpotifyArtistResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Drake");
    }

    @Test
    void findAll_withSearch_shouldPassSearchToRepository() {
        SpotifyArtist artist = buildArtistMock();
        Page<SpotifyArtist> repoPage = new PageImpl<>(List.of(artist), PageRequest.of(0, 20), 1);
        when(artistRepository.findArtists("Drake", PageRequest.of(0, 20))).thenReturn(repoPage);

        Page<SpotifyArtistResponseDto> result = service.findAll("Drake", PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Drake");
    }
}
