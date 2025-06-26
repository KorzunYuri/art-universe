package yurykorzun.art.universe.music.data.approved.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.ArtistSearchResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.ArtistService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    @Autowired
    private MockMvc mockMvc;

    private List<TestBoundEntityProjectionImpl> mockArtistBindings;

    @BeforeEach
    void setup() {
        TestBoundEntityProjectionImpl binding1 = new TestBoundEntityProjectionImpl(123L, DataSource.LASTFM, 321L, "artist1");
        TestBoundEntityProjectionImpl binding2 = new TestBoundEntityProjectionImpl(456L, DataSource.LASTFM, 654L, "artist2");
        TestBoundEntityProjectionImpl binding3 = new TestBoundEntityProjectionImpl(789L, DataSource.SPOTIFY, 987L, "artist3");
        mockArtistBindings = List.of(binding1, binding2, binding3);
    }

    @Test
    void whenFindBoundAlbums_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(emptyList));
        
        when(artistService.findBoundArtists(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When/Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", "999,888"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundArtists_withMatchingArtists_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> expectedArtists = mockArtistBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedArtists.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedArtists));

        when(artistService.findBoundArtists(dataSource, externalIds)).thenReturn(expectedArtists);

        // When & Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundArtists_withSingleMatchingArtist_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<BoundEntityProjection> expectedArtists = mockArtistBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedArtists.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedArtists));

        when(artistService.findBoundArtists(dataSource, externalIds)).thenReturn(expectedArtists);

        // When & Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturnError_whenServiceFails() throws Exception {
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
    
    @Test
    void whenSearchArtists_shouldReturnMatchingArtists() throws Exception {
        // Given
        String searchQuery = "radio";
        ArtistSearchResultDTO artist1 = new ArtistSearchResultDTO(1L, "Radiohead");
        ArtistSearchResultDTO artist2 = new ArtistSearchResultDTO(2L, "Radio Moscow");
        List<ArtistSearchResultDTO> expectedArtists = List.of(artist1, artist2);
        
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedArtists));
        
        when(artistService.searchArtistsByName(searchQuery)).thenReturn(expectedArtists);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/search")
                .param("query", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenSearchArtists_withLimit_shouldReturnLimitedResults() throws Exception {
        // Given
        String searchQuery = "radio";
        Integer limit = 5;
        ArtistSearchResultDTO artist1 = new ArtistSearchResultDTO(1L, "Radiohead");
        ArtistSearchResultDTO artist2 = new ArtistSearchResultDTO(2L, "Radio Moscow");
        List<ArtistSearchResultDTO> expectedArtists = List.of(artist1, artist2);
        
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedArtists));
        
        when(artistService.searchArtistsByName(searchQuery, limit)).thenReturn(expectedArtists);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/search")
                .param("query", searchQuery)
                .param("limit", limit.toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenSearchArtists_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        String searchQuery = "nonexistent";
        List<ArtistSearchResultDTO> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(emptyList));
        
        when(artistService.searchArtistsByName(searchQuery)).thenReturn(emptyList);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/search")
                .param("query", searchQuery))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenSearchArtists_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "radio";
        String errorMessage = "Search error occurred";
        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody(String.format("Failed to search artists: %s", errorMessage)));
        
        when(artistService.searchArtistsByName(searchQuery))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/search")
                .param("query", searchQuery))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }
}
