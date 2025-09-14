package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.ArtistSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ArtistController.class)
class ArtistControllerMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    @MockitoBean
    private BindingService bindingService;

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
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
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
        String expectedJson = objectMapper.writeValueAsString(expectedArtists);

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
        String expectedJson = objectMapper.writeValueAsString(expectedArtists);

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

        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/artists/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenLookupArtists_shouldReturnMatchingArtists() throws Exception {
        // Given
        String searchQuery = "radio";
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        String expectedJson = objectMapper.writeValueAsString(expectedArtists);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupArtists_withLimit_shouldReturnLimitedResults() throws Exception {
        // Given
        String searchQuery = "radio";
        Integer limit = 5;
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        String expectedJson = objectMapper.writeValueAsString(expectedArtists);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", searchQuery)
                .param("limit", limit.toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupArtists_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        String searchQuery = "nonexistent";
        List<LookupResultDTO> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(emptyList);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", searchQuery))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupArtists_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "radio";
        String errorMessage = "Search error occurred";
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/lookup")
                .param("search", searchQuery))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenFindArtists_shouldReturnPageOfArtists() throws Exception {
        // Given
        String searchQuery = "radio";
        ArtistDto artist1 = ArtistDto.builder().id(1L).name("Radiohead").build();
        ArtistDto artist2 = ArtistDto.builder().id(2L).name("Radio Moscow").build();
        List<ArtistDto> artists = List.of(artist1, artist2);
        Page<ArtistDto> expectedPage = new PageImpl<>(artists, PageRequest.of(0, 20), artists.size());
        
        String expectedJson = objectMapper.writeValueAsString(expectedPage);
        
        when(artistService.findArtists(eq(searchQuery), any(Pageable.class))).thenReturn(expectedPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .param("search", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenFindArtists_withNoSearch_shouldReturnAllArtists() throws Exception {
        // Given
        ArtistDto artist = ArtistDto.builder().id(1L).name("Test Artist").build();
        Page<ArtistDto> expectedPage = new PageImpl<>(List.of(artist), PageRequest.of(0, 20), 1);
        
        String expectedJson = objectMapper.writeValueAsString(expectedPage);
        
        when(artistService.findArtists(eq(null), any(Pageable.class))).thenReturn(expectedPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenFindArtists_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "radio";
        String errorMessage = "Search error occurred";
        
        when(artistService.findArtists(eq(searchQuery), any(Pageable.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists")
                .param("search", searchQuery))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenSaveArtist_shouldReturnSavedArtist() throws Exception {
        // Given
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .name("New Artist")
            .build();
        
        ArtistDto savedArtist = ArtistDto.builder()
            .id(1L)
            .name("New Artist")
            .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        String expectedJson = objectMapper.writeValueAsString(savedArtist);
        
        when(artistService.saveArtist(any(ArtistSaveRequestDTO.class))).thenReturn(savedArtist);
        
        // When & Then
        mockMvc.perform(post("/api/v1/artists")
                .contentType("application/json")
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenSaveArtist_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .name("New Artist")
            .build();
        
        String requestJson = objectMapper.writeValueAsString(request);
        String errorMessage = "Save error occurred";
        
        when(artistService.saveArtist(any(ArtistSaveRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(post("/api/v1/artists")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenDeleteArtist_shouldReturnTrue() throws Exception {
        // Given
        Long id = 1L;
        
        when(artistService.deleteArtist(id)).thenReturn(true);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/artists/{id}", id))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
    
    @Test
    void whenDeleteArtist_notFound_shouldReturnNotFound() throws Exception {
        // Given
        Long id = 1L;
        
        when(artistService.deleteArtist(id)).thenReturn(false);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/artists/{id}", id))
            .andExpect(status().isNotFound());
    }
    
    @Test
    void whenDeleteArtist_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        Long id = 1L;
        String errorMessage = "Delete error occurred";
        
        when(artistService.deleteArtist(id))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(delete("/api/v1/artists/{id}", id))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenGetArtist_shouldReturnArtist() throws Exception {
        // Given
        Long id = 1L;
        ArtistDto artist = ArtistDto.builder().id(id).name("Test Artist").build();
        
        String expectedJson = objectMapper.writeValueAsString(artist);
        
        when(artistService.getArtist(id)).thenReturn(artist);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/{id}", id))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenGetArtist_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        Long id = 1L;
        String errorMessage = "Artist not found";
        
        when(artistService.getArtist(id))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/{id}", id))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenFindArtistsWithCategories_shouldReturnPageOfArtistsWithCategories() throws Exception {
        // Given
        String searchQuery = "radio";
        ArtistWithCategoriesDto artist = ArtistWithCategoriesDto.builder()
            .id(1L)
            .name("Radiohead")
            .categories(List.of())
            .build();
        List<ArtistWithCategoriesDto> artists = List.of(artist);
        Page<ArtistWithCategoriesDto> expectedPage = new PageImpl<>(artists, PageRequest.of(0, 20), artists.size());
        
        String expectedJson = objectMapper.writeValueAsString(expectedPage);
        
        when(artistService.findArtistsWithCategories(eq(searchQuery), any(Pageable.class))).thenReturn(expectedPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/with-categories")
                .param("search", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenFindArtistsWithCategories_withNoSearch_shouldReturnAllArtists() throws Exception {
        // Given
        ArtistWithCategoriesDto artist = ArtistWithCategoriesDto.builder()
            .id(1L)
            .name("Test Artist")
            .categories(List.of())
            .build();
        Page<ArtistWithCategoriesDto> expectedPage = new PageImpl<>(List.of(artist), PageRequest.of(0, 20), 1);
        
        String expectedJson = objectMapper.writeValueAsString(expectedPage);
        
        when(artistService.findArtistsWithCategories(eq(null), any(Pageable.class))).thenReturn(expectedPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/with-categories"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenFindArtistsWithCategories_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "radio";
        String errorMessage = "Search error occurred";
        
        when(artistService.findArtistsWithCategories(eq(searchQuery), any(Pageable.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/artists/with-categories")
                .param("search", searchQuery))
            .andExpect(status().isInternalServerError());
    }
}
