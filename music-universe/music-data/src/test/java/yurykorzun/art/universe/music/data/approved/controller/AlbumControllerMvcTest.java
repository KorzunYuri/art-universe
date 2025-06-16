package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.AlbumService;

import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlbumController.class)
public class AlbumControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlbumService albumService;

    @Test
    void whenFindBoundAlbums_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        when(albumService.findBoundAlbums(eq(DataSource.LASTFM), any()))
            .thenReturn(Collections.emptyList());

        // When/Then
        mockMvc.perform(get("/api/v1/albums/bound/LASTFM")
                .param("externalIds", "999,888"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success", is(true)))
            .andExpect(jsonPath("$.data", hasSize(0)));
    }

    @Test
    void whenFindBoundAlbums_withException_shouldReturnFailureResponse() throws Exception {
        // Given
        when(albumService.findBoundAlbums(eq(DataSource.LASTFM), any()))
            .thenThrow(new RuntimeException("Test exception"));

        // When/Then
        mockMvc.perform(get("/api/v1/albums/bound/LASTFM")
                .param("externalIds", "101,102"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success", is(false)))
            .andExpect(jsonPath("$.message", containsString("Test exception")))
            .andExpect(jsonPath("$.data").doesNotExist());
    }
}
