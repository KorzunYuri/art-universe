package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.BindingService;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataMvcTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlbumController.class)
class AlbumControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private BindingService bindingService;

    private List<TestBoundEntityProjectionImpl> mockAlbumBindings;

    @BeforeEach
    void setup() {
        TestBoundEntityProjectionImpl binding1 = new TestBoundEntityProjectionImpl(201L, DataSource.LASTFM, 1L, "OK Computer");
        TestBoundEntityProjectionImpl binding2 = new TestBoundEntityProjectionImpl(202L, DataSource.LASTFM, 2L, "Kid A");
        TestBoundEntityProjectionImpl binding3 = new TestBoundEntityProjectionImpl(203L, DataSource.SPOTIFY, 3L, "In Rainbows");
        mockAlbumBindings = List.of(binding1, binding2, binding3);
    }

    @Test
    void whenFindBoundAlbums_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
        when(albumService.findBoundAlbums(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When/Then
        mockMvc.perform(get("/api/v1/albums/bound/{dataSource}", dataSource)
                .param("externalIds", "999,888"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundAlbums_withMatchingAlbums_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> expectedAlbums = mockAlbumBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedAlbums.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedAlbums);

        when(albumService.findBoundAlbums(dataSource, externalIds)).thenReturn(expectedAlbums);

        // When & Then
        mockMvc.perform(get("/api/v1/albums/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundAlbums_withSingleMatchingAlbum_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<BoundEntityProjection> expectedAlbums = mockAlbumBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedAlbums.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedAlbums);

        when(albumService.findBoundAlbums(dataSource, externalIds)).thenReturn(expectedAlbums);

        // When & Then
        mockMvc.perform(get("/api/v1/albums/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturnError_whenServiceFails() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(201L, 202L);
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String errorMessage = "Service error occurred";

        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/albums/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andExpect(status().isInternalServerError());
    }

    // --- POST /albums (saveAlbum) ---

    @Test
    void saveAlbum_withValidRequest_shouldReturn200WithBody() throws Exception {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .build();
        AlbumDto response = AlbumDto.builder().id(1L).name("OK Computer").primaryArtistId(10L).build();

        when(albumService.saveAlbum(any(AlbumSaveRequestDTO.class), any(Origin.class), any(MasterApprovalStatus.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void saveAlbum_withMissingName_shouldReturn400() throws Exception {
        // Given — no 'name' field → @NotBlank fails
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .primaryArtistId(10L)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // --- DELETE /albums/{id} (deleteAlbum) ---

    @Test
    void deleteAlbum_whenFound_shouldReturn200() throws Exception {
        // Given
        when(albumService.deleteAlbum(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/api/v1/albums/{id}", 1L))
            .andExpect(status().isOk());
    }

    @Test
    void deleteAlbum_whenNotFound_shouldReturn404() throws Exception {
        // Given — service returns false → controller throws CustomEntityNotFoundException → 404
        when(albumService.deleteAlbum(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/api/v1/albums/{id}", 999L))
            .andExpect(status().isNotFound());
    }

    // --- POST /albums/with-tracks (saveAlbumWithTracks) ---

    @Test
    void saveAlbumWithTracks_withValidRequest_shouldReturn200() throws Exception {
        // Given
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(1L).trackOrder(1).build(),
                AlbumTrackItemDTO.builder().trackId(2L).trackOrder(2).build()
            ))
            .build();
        AlbumDto response = AlbumDto.builder().id(1L).name("OK Computer").primaryArtistId(10L).build();

        when(albumService.saveAlbumWithTracks(any(AlbumWithTracksSaveRequestDTO.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/with-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void saveAlbumWithTracks_withMissingName_shouldReturn400() throws Exception {
        // Given
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .primaryArtistId(10L)
            .tracks(List.of(AlbumTrackItemDTO.builder().trackId(1L).trackOrder(1).build()))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/with-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void saveAlbumWithTracks_withEmptyTracks_shouldReturn400() throws Exception {
        // Given
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .tracks(List.of())
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/with-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void saveAlbumWithTracks_withTrackMissingOrder_shouldReturn400() throws Exception {
        // Given — trackOrder is null → @NotNull fails
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .tracks(List.of(AlbumTrackItemDTO.builder().trackId(1L).build()))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/with-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void saveAlbumWithTracks_withTrackMissingId_shouldReturn400() throws Exception {
        // Given — trackId is null → @NotNull fails
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .tracks(List.of(AlbumTrackItemDTO.builder().trackOrder(1).build()))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/with-tracks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // --- POST /albums/bind/new/{dataSource}/{externalId} (createAndBind) ---

    @Test
    void createAndBind_withValidRequest_shouldReturn200() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 500L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("OK Computer")
            .masterPrimaryArtistId(100L)
            .build();
        BoundEntityProjection response = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBind(eq(dataSource), eq(externalId), any(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}", dataSource, externalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createAndBind_withMissingEntityName_shouldReturn400() throws Exception {
        // Given — entityName is null → @NotBlank fails
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 500L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(100L)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}", dataSource, externalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    // --- POST /albums/bind/new/{dataSource}/{externalId}/with-tracks (createAndBindWithTracks) ---

    @Test
    void createAndBindWithTracks_withUnboundTracks_shouldReturn200() throws Exception {
        // Given — all tracks are unbound (have trackName, no masterTrackId)
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().externalTrackId(10L).trackName("Airbag").trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder().externalTrackId(20L).trackName("Paranoid Android").trackOrder(2).build()
            ))
            .build();
        BoundEntityProjection response = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(eq(dataSource), eq(externalAlbumId), any(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createAndBindWithTracks_withBoundTracks_shouldReturn200() throws Exception {
        // Given — all tracks are bound (have masterTrackId, no trackName)
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().externalTrackId(10L).masterTrackId(101L).trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder().externalTrackId(20L).masterTrackId(201L).trackOrder(2).build()
            ))
            .build();
        BoundEntityProjection response = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(eq(dataSource), eq(externalAlbumId), any(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));
    }

    @Test
    void createAndBindWithTracks_withMixedTracks_shouldReturn200() throws Exception {
        // Given — mix of bound and unbound tracks; one track has a relation type override
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().externalTrackId(10L).trackName("Airbag").trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder().externalTrackId(20L).masterTrackId(201L).trackOrder(2).relationTypeId(50L).build(),
                ExternalAlbumTrackItemDTO.builder().externalTrackId(30L).trackName("Karma Police").trackOrder(3).build()
            ))
            .build();
        BoundEntityProjection response = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(eq(dataSource), eq(externalAlbumId), any(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk());
    }

    @Test
    void createAndBindWithTracks_withPerTrackArtistOverride_shouldDeserializeAndPassToService() throws Exception {
        // Given — second track carries a per-track masterPrimaryArtistId; first track has none
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        Long trackArtistOverrideId = 200L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(10L).trackName("Airbag").trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(20L).trackName("Paranoid Android").trackOrder(2)
                    .masterPrimaryArtistId(trackArtistOverrideId)
                    .build()
            ))
            .build();
        BoundEntityProjection response = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(eq(dataSource), eq(externalAlbumId), any(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)))
            .thenReturn(response);

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(response)));

        // Verify the field survived JSON deserialization and reached the service
        verify(albumService).createAndBindWithTracks(
            eq(dataSource),
            eq(externalAlbumId),
            argThat(req ->
                trackArtistOverrideId.equals(req.getTracks().get(1).getMasterPrimaryArtistId()) &&
                req.getTracks().getFirst().getMasterPrimaryArtistId() == null
            ),
            eq(Origin.MANUAL),
            eq(MasterApprovalStatus.APPROVED)
        );
    }

    @Test
    void createAndBindWithTracks_withEmptyTracks_shouldReturn400() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of())
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createAndBindWithTracks_withTrackMissingExternalId_shouldReturn400() throws Exception {
        // Given — externalTrackId is null → @NotNull fails
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().trackName("Airbag").trackOrder(1).build()
            ))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createAndBindWithTracks_withTrackMissingOrder_shouldReturn400() throws Exception {
        // Given — trackOrder is null → @NotNull fails
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().externalTrackId(10L).trackName("Airbag").build()
            ))
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalAlbumId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest());
    }
}
