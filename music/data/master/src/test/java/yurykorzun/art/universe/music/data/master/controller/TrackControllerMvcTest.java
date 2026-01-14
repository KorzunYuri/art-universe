package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.BindingService;
import yurykorzun.art.universe.music.data.master.service.TrackService;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataMvcTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackController.class)
class TrackControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrackService trackService;

    @MockitoBean
    private BindingService bindingService;

    @Autowired
    private MockMvc mockMvc;

    private List<TestBoundEntityProjectionImpl> mockTrackBindings;

    @BeforeEach
    void setup() {
        TestBoundEntityProjectionImpl binding1 = new TestBoundEntityProjectionImpl(101L, DataSource.LASTFM, 1L, "Paranoid Android");
        TestBoundEntityProjectionImpl binding2 = new TestBoundEntityProjectionImpl(102L, DataSource.LASTFM, 2L, "Karma Police");
        TestBoundEntityProjectionImpl binding3 = new TestBoundEntityProjectionImpl(103L, DataSource.SPOTIFY, 3L, "No Surprises");
        mockTrackBindings = List.of(binding1, binding2, binding3);
    }

    @Test
    void whenFindBoundTracks_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
        when(trackService.findBoundTracks(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When/Then
        mockMvc.perform(get("/api/v1/tracks/bound/{dataSource}", dataSource)
                .param("externalIds", "999,888"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundTracks_withMatchingTracks_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> expectedTracks = mockTrackBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedTracks.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedTracks);

        when(trackService.findBoundTracks(dataSource, externalIds)).thenReturn(expectedTracks);

        // When & Then
        mockMvc.perform(get("/api/v1/tracks/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundTracks_withSingleMatchingTrack_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<BoundEntityProjection> expectedTracks = mockTrackBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedTracks.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedTracks);

        when(trackService.findBoundTracks(dataSource, externalIds)).thenReturn(expectedTracks);

        // When & Then
        mockMvc.perform(get("/api/v1/tracks/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturnError_whenServiceFails() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String errorMessage = "Service error occurred";

        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/tracks/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andExpect(status().isInternalServerError());
    }
}
