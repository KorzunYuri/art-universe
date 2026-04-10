package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import java.util.List;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.CategoryService;
import yurykorzun.art.universe.music.data.master.service.TrackService;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataMvcTest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalEntityController.class)
class InternalEntityControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ArtistService artistService;

    @MockitoBean
    private AlbumService albumService;

    @MockitoBean
    private TrackService trackService;

    @MockitoBean
    private CategoryService categoryService;

    // --- Artist endpoints ---

    @Test
    void createArtist_shouldReturnArtistDto() throws Exception {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("New Artist").build();
        ArtistDto savedArtist = ArtistDto.builder().id(1L).name("New Artist").build();

        when(artistService.saveArtist(any(ArtistSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.NEW)))
            .thenReturn(savedArtist);

        mockMvc.perform(post("/api/v1/internal/artists")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(savedArtist)));
    }

    @Test
    void createArtist_withCustomApprovalStatus_shouldPassIt() throws Exception {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("New Artist").build();
        ArtistDto savedArtist = ArtistDto.builder().id(1L).name("New Artist").build();

        when(artistService.saveArtist(any(ArtistSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.APPROVED)))
            .thenReturn(savedArtist);

        mockMvc.perform(post("/api/v1/internal/artists")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request))
                .param("approvalStatus", "approved"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(savedArtist)));

        verify(artistService).saveArtist(any(ArtistSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.APPROVED));
    }

    @Test
    void createArtist_whenError_shouldReturnInternalServerError() throws Exception {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("New Artist").build();

        when(artistService.saveArtist(any(ArtistSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.NEW)))
            .thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/internal/artists")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void bindArtistToExisting_shouldReturnBoundEntityProjection() throws Exception {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 123L;
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder().masterId(1L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Artist");

        when(artistService.bindToExisting(eq(dataSource), eq(externalId), any(EntityBindToExistingRequestDTO.class),
            eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)))
            .thenReturn(projection);

        mockMvc.perform(post("/api/v1/internal/artists/bind/existing/{dataSource}/{externalId}", dataSource, externalId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(projection)));
    }

    @Test
    void createAndBindArtist_shouldReturnBoundEntityProjection() throws Exception {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 123L;
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder().entityName("Artist").build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Artist");

        when(artistService.createAndBind(eq(dataSource), eq(externalId), any(EntityCreateAndBindRequestDTO.class),
            eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)))
            .thenReturn(projection);

        mockMvc.perform(post("/api/v1/internal/artists/bind/new/{dataSource}/{externalId}", dataSource, externalId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(projection)));
    }

    // --- Album endpoints ---

    @Test
    void createAlbum_shouldReturnAlbumDto() throws Exception {
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder().name("New Album").primaryArtistId(1L).build();
        AlbumDto savedAlbum = AlbumDto.builder().id(1L).name("New Album").build();

        when(albumService.saveAlbum(any(AlbumSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.NEW)))
            .thenReturn(savedAlbum);

        mockMvc.perform(post("/api/v1/internal/albums")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(savedAlbum)));
    }

    @Test
    void createAndBindAlbumWithTracks_shouldReturnBoundEntityProjection() throws Exception {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 456L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("Album").masterPrimaryArtistId(2L)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(100L).trackName("Track 1").trackOrder(1).build()))
            .build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Album");

        when(albumService.createAndBindWithTracks(eq(dataSource), eq(externalId),
            any(ExternalAlbumWithTracksCreateAndBindRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)))
            .thenReturn(projection);

        mockMvc.perform(post("/api/v1/internal/albums/bind/new/{dataSource}/{externalId}/with-tracks", dataSource, externalId)
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(projection)));
    }

    // --- Track endpoints ---

    @Test
    void createTrack_shouldReturnTrackDto() throws Exception {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder().name("New Track").primaryArtistId(1L).build();
        TrackDto savedTrack = TrackDto.builder().id(1L).name("New Track").build();

        when(trackService.saveTrack(any(TrackSaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.NEW)))
            .thenReturn(savedTrack);

        mockMvc.perform(post("/api/v1/internal/tracks")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(savedTrack)));
    }

    // --- Category endpoints ---

    @Test
    void createCategory_shouldReturnCategoryDto() throws Exception {
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder().name("New Category").build();
        CategoryDto savedCategory = CategoryDto.builder().id(1L).name("New Category").build();

        when(categoryService.saveCategory(any(CategorySaveRequestDTO.class), eq(Origin.ETL), eq(MasterApprovalStatus.NEW)))
            .thenReturn(savedCategory);

        mockMvc.perform(post("/api/v1/internal/categories")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(savedCategory)));
    }
}
