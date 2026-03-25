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
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
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
class SpotifyTrackServiceImplTest {

    @Mock
    private SpotifyTrackRepository trackRepository;

    @Mock
    private SpotifyArtistRepository artistRepository;

    @InjectMocks
    private SpotifyTrackServiceImpl service;

    private SpotifyTrack buildTrackMock(Long artistId) {
        SpotifyTrack track = mock(SpotifyTrack.class);
        when(track.getId()).thenReturn(1L);
        when(track.getSpotifyId()).thenReturn("4cluDES4hQEUhmXj6TXkSo");
        when(track.getName()).thenReturn("God's Plan");
        when(track.getDurationMs()).thenReturn(211000);
        when(track.getTrackNumber()).thenReturn(1);
        when(track.getPrimaryArtistId()).thenReturn(artistId);
        when(track.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
        return track;
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
        SpotifyTrack track = buildTrackMock(10L);
        SpotifyArtist artist = buildArtistMock("Drake");
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));
        when(artistRepository.findById(10L)).thenReturn(Optional.of(artist));

        SpotifyTrackResponseDto dto = service.findById(1L);

        assertThat(dto.id()).isEqualTo(1L);
        assertThat(dto.name()).isEqualTo("God's Plan");
        assertThat(dto.primaryArtistName()).isEqualTo("Drake");
    }

    @Test
    void findById_shouldReturnDtoWithNullArtistName_whenNoArtist() {
        SpotifyTrack track = buildTrackMock(null);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        SpotifyTrackResponseDto dto = service.findById(1L);

        assertThat(dto.primaryArtistName()).isNull();
    }

    @Test
    void findById_shouldThrowEntityNotFoundException_whenNotFound() {
        when(trackRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(999L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("999");
    }

    @Test
    void findAll_shouldReturnMappedPageWithArtistNames() {
        SpotifyTrack track = buildTrackMock(10L);
        SpotifyArtist artist = buildArtistMockWithId(10L, "Drake");
        Page<SpotifyTrack> repoPage = new PageImpl<>(List.of(track), PageRequest.of(0, 20), 1);
        when(trackRepository.findTracks(isNull(), any())).thenReturn(repoPage);
        when(artistRepository.findAllById(List.of(10L))).thenReturn(List.of(artist));

        Page<SpotifyTrackResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("God's Plan");
        assertThat(result.getContent().get(0).primaryArtistName()).isEqualTo("Drake");
    }

    @Test
    void findAll_shouldHandleTracksWithoutArtistId() {
        SpotifyTrack track = buildTrackMock(null);
        Page<SpotifyTrack> repoPage = new PageImpl<>(List.of(track), PageRequest.of(0, 20), 1);
        when(trackRepository.findTracks(isNull(), any())).thenReturn(repoPage);

        Page<SpotifyTrackResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).primaryArtistName()).isNull();
    }

    @Test
    void findAll_shouldBatchResolveArtistNames() {
        SpotifyTrack track1 = buildTrackMock(10L);
        SpotifyTrack track2 = mock(SpotifyTrack.class);
        when(track2.getId()).thenReturn(2L);
        when(track2.getSpotifyId()).thenReturn("another-id");
        when(track2.getName()).thenReturn("Hotline Bling");
        when(track2.getPrimaryArtistId()).thenReturn(10L);
        when(track2.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);

        SpotifyArtist artist = buildArtistMockWithId(10L, "Drake");
        Page<SpotifyTrack> repoPage = new PageImpl<>(List.of(track1, track2), PageRequest.of(0, 20), 2);
        when(trackRepository.findTracks(isNull(), any())).thenReturn(repoPage);
        when(artistRepository.findAllById(List.of(10L))).thenReturn(List.of(artist));

        Page<SpotifyTrackResponseDto> result = service.findAll(null, PageRequest.of(0, 20));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).primaryArtistName()).isEqualTo("Drake");
        assertThat(result.getContent().get(1).primaryArtistName()).isEqualTo("Drake");
    }
}
