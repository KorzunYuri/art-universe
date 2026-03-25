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
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.dto.SpotifyAlbumTrackResponseDto;
import yurykorzun.art.universe.music.data.raw.spotify.domain.service.SpotifyAlbumService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyAlbumController.class)
class SpotifyAlbumControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private SpotifyAlbumService albumService;

    private SpotifyAlbumResponseDto albumDto;

    @BeforeEach
    void setUp() {
        albumDto = new SpotifyAlbumResponseDto(
                1L,
                "3TVXtAsR1Inumwj472S9r4",
                "Certified Lover Boy",
                null, 21, "2021-09-03", null,
                "https://open.spotify.com/album/3TVXtAsR1Inumwj472S9r4",
                null, 10L, "artist-spotify-id", "Drake", 0
        );
    }

    @Test
    void GET_albums_shouldReturnPageOfAlbums() throws Exception {
        Page<SpotifyAlbumResponseDto> page = new PageImpl<>(List.of(albumDto), PageRequest.of(0, 20), 1);
        when(albumService.findAll(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/albums")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Certified Lover Boy"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("3TVXtAsR1Inumwj472S9r4"))
            .andExpect(jsonPath("$.content[0].primaryArtistName").value("Drake"));
    }

    @Test
    void GET_albums_withSearch_shouldPassSearchParamToService() throws Exception {
        Page<SpotifyAlbumResponseDto> page = new PageImpl<>(List.of(albumDto), PageRequest.of(0, 20), 1);
        when(albumService.findAll(eq("Lover"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/albums")
                .param("search", "Lover")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Certified Lover Boy"));
    }

    @Test
    void GET_albumById_shouldReturnAlbum_whenFound() throws Exception {
        when(albumService.findById(1L)).thenReturn(albumDto);

        mockMvc.perform(get("/api/v1/spotify/albums/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Certified Lover Boy"))
            .andExpect(jsonPath("$.spotifyId").value("3TVXtAsR1Inumwj472S9r4"))
            .andExpect(jsonPath("$.primaryArtistName").value("Drake"));
    }

    @Test
    void GET_albumById_shouldReturn404_whenNotFound() throws Exception {
        when(albumService.findById(999L))
            .thenThrow(new EntityNotFoundException("Album not found with id: 999"));

        mockMvc.perform(get("/api/v1/spotify/albums/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    void GET_albumTracks_shouldReturnTracklist() throws Exception {
        SpotifyAlbumTrackResponseDto trackDto = new SpotifyAlbumTrackResponseDto(
                100L, 1, 100L, "Champagne Poetry",
                "track-spotify-id", "https://open.spotify.com/track/track-spotify-id",
                10L, 287000
        );
        when(albumService.findAlbumTracks(1L)).thenReturn(List.of(trackDto));

        mockMvc.perform(get("/api/v1/spotify/albums/1/tracks")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].trackId").value(100))
            .andExpect(jsonPath("$[0].trackName").value("Champagne Poetry"))
            .andExpect(jsonPath("$[0].trackNumber").value(1))
            .andExpect(jsonPath("$[0].durationMs").value(287000));
    }
}
