package yurykorzun.art.universe.music.data.raw.spotify.controller;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyTrackService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyTrackController.class)
class SpotifyTrackControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private SpotifyTrackService trackService;

    private SpotifyTrackResponseDto trackDto;

    @BeforeEach
    void setUp() {
        trackDto = new SpotifyTrackResponseDto(
                1L,
                "4cluDES4hQEUhmXj6TXkSo",
                "God's Plan",
                211000, 1, 1,
                false, true,
                "https://open.spotify.com/track/4cluDES4hQEUhmXj6TXkSo",
                null, null,
                10L, "artist-spotify-id", "Drake",
                20L, "album-spotify-id",
                0
        );
    }

    @Test
    void GET_tracks_shouldReturnPageOfTracks() throws Exception {
        Page<SpotifyTrackResponseDto> page = new PageImpl<>(List.of(trackDto), PageRequest.of(0, 20), 1);
        when(trackService.findAll(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/tracks")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("God's Plan"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("4cluDES4hQEUhmXj6TXkSo"))
            .andExpect(jsonPath("$.content[0].primaryArtistName").value("Drake"));
    }

    @Test
    void GET_tracks_withSearch_shouldPassSearchParamToService() throws Exception {
        Page<SpotifyTrackResponseDto> page = new PageImpl<>(List.of(trackDto), PageRequest.of(0, 20), 1);
        when(trackService.findAll(eq("God"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/tracks")
                .param("search", "God")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("God's Plan"));
    }

    @Test
    void GET_trackById_shouldReturnTrack_whenFound() throws Exception {
        when(trackService.findById(1L)).thenReturn(trackDto);

        mockMvc.perform(get("/api/v1/spotify/tracks/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("God's Plan"))
            .andExpect(jsonPath("$.spotifyId").value("4cluDES4hQEUhmXj6TXkSo"))
            .andExpect(jsonPath("$.primaryArtistName").value("Drake"));
    }

    @Test
    void GET_trackById_shouldReturn404_whenNotFound() throws Exception {
        when(trackService.findById(999L))
            .thenThrow(new EntityNotFoundException("Track not found with id: 999"));

        mockMvc.perform(get("/api/v1/spotify/tracks/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
