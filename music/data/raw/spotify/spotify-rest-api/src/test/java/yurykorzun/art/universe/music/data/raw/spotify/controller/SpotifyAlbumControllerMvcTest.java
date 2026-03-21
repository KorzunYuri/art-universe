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
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyAlbumRepository;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyAlbumController.class)
class SpotifyAlbumControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private SpotifyAlbumRepository albumRepository;

    private SpotifyAlbum albumMock;

    @BeforeEach
    void setUp() {
        albumMock = mock(SpotifyAlbum.class);
        when(albumMock.getId()).thenReturn(1L);
        when(albumMock.getSpotifyId()).thenReturn("3TVXtAsR1Inumwj472S9r4");
        when(albumMock.getName()).thenReturn("Certified Lover Boy");
        when(albumMock.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
    }

    @Test
    void GET_albums_shouldReturnPageOfAlbums() throws Exception {
        Page<SpotifyAlbum> page = new PageImpl<>(List.of(albumMock), PageRequest.of(0, 20), 1);
        when(albumRepository.findAlbums(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/albums")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("Certified Lover Boy"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("3TVXtAsR1Inumwj472S9r4"));
    }

    @Test
    void GET_albums_withSearch_shouldPassSearchParamToRepository() throws Exception {
        Page<SpotifyAlbum> page = new PageImpl<>(List.of(albumMock), PageRequest.of(0, 20), 1);
        when(albumRepository.findAlbums(eq("Lover"), any())).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/albums")
                .param("search", "Lover")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Certified Lover Boy"));
    }

    @Test
    void GET_albumById_shouldReturnAlbum_whenFound() throws Exception {
        when(albumRepository.findById(1L)).thenReturn(Optional.of(albumMock));

        mockMvc.perform(get("/api/v1/spotify/albums/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Certified Lover Boy"))
            .andExpect(jsonPath("$.spotifyId").value("3TVXtAsR1Inumwj472S9r4"));
    }

    @Test
    void GET_albumById_shouldReturn404_whenNotFound() throws Exception {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/spotify/albums/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
