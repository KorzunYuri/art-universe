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
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyGenre;
import yurykorzun.art.universe.music.data.raw.spotify.domain.repository.SpotifyGenreRepository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SpotifyGenreController.class)
class SpotifyGenreControllerMvcTest extends BaseMvcTest {

    @MockitoBean
    private SpotifyGenreRepository genreRepository;

    private SpotifyGenre genreMock;

    @BeforeEach
    void setUp() {
        genreMock = mock(SpotifyGenre.class);
        when(genreMock.getId()).thenReturn(1L);
        when(genreMock.getSpotifyId()).thenReturn("genre-hip-hop");
        when(genreMock.getName()).thenReturn("hip-hop");
        when(genreMock.getApprovalStatus()).thenReturn(ApprovalStatus.PENDING);
    }

    @Test
    void GET_genres_shouldReturnPageOfGenres() throws Exception {
        Page<SpotifyGenre> page = new PageImpl<>(List.of(genreMock), PageRequest.of(0, 20), 1);
        when(genreRepository.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/spotify/genres")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.content[0].id").value(1))
            .andExpect(jsonPath("$.content[0].name").value("hip-hop"))
            .andExpect(jsonPath("$.content[0].spotifyId").value("genre-hip-hop"));
    }

    @Test
    void GET_genreById_shouldReturnGenre_whenFound() throws Exception {
        when(genreRepository.findById(1L)).thenReturn(Optional.of(genreMock));

        mockMvc.perform(get("/api/v1/spotify/genres/1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("hip-hop"))
            .andExpect(jsonPath("$.spotifyId").value("genre-hip-hop"));
    }

    @Test
    void GET_genreById_shouldReturn404_whenNotFound() throws Exception {
        when(genreRepository.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/spotify/genres/999")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
