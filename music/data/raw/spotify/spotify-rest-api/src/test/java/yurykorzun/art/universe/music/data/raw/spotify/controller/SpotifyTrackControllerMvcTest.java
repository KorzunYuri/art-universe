package yurykorzun.art.universe.music.data.raw.spotify.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyTrackRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyTrackController.class)
class SpotifyTrackControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private SpotifyTrackRepository trackRepository;

    private SpotifyTrack trackMock;

    @BeforeEach
    void setUp() {
        trackMock = mock(SpotifyTrack.class);
        when(trackMock.getId()).thenReturn(1L);
        when(trackMock.getSpotifyId()).thenReturn("4cluDES4hQEUhmXj6TXkSo");
        when(trackMock.getName()).thenReturn("God's Plan");
        when(trackMock.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
    }

    @Test
    void GET_tracks_shouldReturnPageOfTracks() throws Exception {
        Page<SpotifyTrack> page = new PageImpl<>(List.of(trackMock), PageRequest.of(0, 20), 1);
        when(trackRepository.findTracks(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/tracks")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("God's Plan"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("4cluDES4hQEUhmXj6TXkSo"));
    }

    @Test
    void GET_tracks_withSearch_shouldPassSearchParamToRepository() throws Exception {
        Page<SpotifyTrack> page = new PageImpl<>(List.of(trackMock), PageRequest.of(0, 20), 1);
        when(trackRepository.findTracks(eq("God"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/tracks")
                .param("search", "God")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("God's Plan"));
    }

    @Test
    void GET_trackById_shouldReturnTrack_whenFound() throws Exception {
        when(trackRepository.findById(1L)).thenReturn(Optional.of(trackMock));

        mockMvc.perform(get("/api/v1/spotify/tracks/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("God's Plan"))
            .andExpect(jsonPath("$.spotifyId").value("4cluDES4hQEUhmXj6TXkSo"));
    }

    @Test
    void GET_trackById_shouldReturn404_whenNotFound() throws Exception {
        when(trackRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/spotify/tracks/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
