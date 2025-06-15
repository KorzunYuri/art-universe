package yurykorzun.art.universe.music.data.approved.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerMvcTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    private MockMvc mockMvc;

    private List<TestBoundEntityProjectionImpl> mockArtistBindings;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();

        mockArtistBindings = Arrays.asList(
            new TestBoundEntityProjectionImpl(123L, DataSource.LASTFM, 321L, "artist1"),
            new TestBoundEntityProjectionImpl(456L, DataSource.SPOTIFY, 654L, "artist2")
        );
    }

    @Test
    void shouldReturnBoundArtistsSuccessfully() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(123L, 456L);
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(mockArtistBindings));

        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenReturn(new ArrayList<>(mockArtistBindings));

        // When & Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturnErrorWhenServiceFails() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(123L, 456L);
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String errorMessage = "Service error occurred";
        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody(String.format("Failed to get bound artists: %s", errorMessage)));

        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }
}