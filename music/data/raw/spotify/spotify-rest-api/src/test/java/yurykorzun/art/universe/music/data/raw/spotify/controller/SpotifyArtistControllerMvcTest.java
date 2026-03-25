package yurykorzun.art.universe.music.data.raw.spotify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyArtistService;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.lookup.SpotifyArtistLookupService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyArtistController.class)
class SpotifyArtistControllerMvcTest extends BaseMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SpotifyArtistService artistService;

    @MockitoBean
    private SpotifyArtistLookupService artistLookupService;

    private SpotifyArtistResponseDto artistDto;

    @BeforeEach
    void setUp() {
        artistDto = new SpotifyArtistResponseDto(
            1L,
            "3TVXtAsR1Inumwj472S9r4",
            "Drake",
            "https://open.spotify.com/artist/3TVXtAsR1Inumwj472S9r4",
            "spotify:artist:3TVXtAsR1Inumwj472S9r4",
            0
        );
    }

    @Test
    void GET_artists_shouldReturnPageOfArtists() throws Exception {
        Page<SpotifyArtistResponseDto> page = new PageImpl<>(
            List.of(artistDto),
            PageRequest.of(0, 20),
            1
        );
        when(artistService.findAll(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/artists")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Drake"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("3TVXtAsR1Inumwj472S9r4"));
    }

    @Test
    void GET_artists_withSearch_shouldPassSearchParamToService() throws Exception {
        Page<SpotifyArtistResponseDto> page = new PageImpl<>(
            List.of(artistDto),
            PageRequest.of(0, 20),
            1
        );
        when(artistService.findAll(eq("Drake"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/artists")
                .param("search", "Drake")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Drake"));
    }

    @Test
    void GET_artistById_shouldReturnArtist_whenFound() throws Exception {
        when(artistService.findById(1L)).thenReturn(artistDto);

        mockMvc.perform(get("/api/v1/spotify/artists/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Drake"))
            .andExpect(jsonPath("$.spotifyId").value("3TVXtAsR1Inumwj472S9r4"));
    }

    @Test
    void GET_artistById_shouldReturn404_whenNotFound() throws Exception {
        when(artistService.findById(999L))
            .thenThrow(new EntityNotFoundException("Artist not found with id: 999"));

        mockMvc.perform(get("/api/v1/spotify/artists/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
