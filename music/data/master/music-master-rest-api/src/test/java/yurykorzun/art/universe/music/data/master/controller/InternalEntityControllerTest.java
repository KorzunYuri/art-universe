package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.CategoryService;
import yurykorzun.art.universe.music.data.master.service.TrackService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalEntityControllerTest {

    @Mock
    private ArtistService artistService;

    @Mock
    private AlbumService albumService;

    @Mock
    private TrackService trackService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private InternalEntityController controller;

    // --- Artists ---

    @Test
    void createArtist_shouldPassEtlOriginAndDefaultApprovalStatus() {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("Test Artist").build();
        ArtistDto expected = ArtistDto.builder().id(1L).name("Test Artist").build();

        when(artistService.saveArtist(request, Origin.ETL, MasterApprovalStatus.NEW)).thenReturn(expected);

        ArtistDto result = controller.createArtist(request, MasterApprovalStatus.NEW);

        assertEquals(expected, result);
        verify(artistService).saveArtist(request, Origin.ETL, MasterApprovalStatus.NEW);
    }

    @Test
    void createArtist_withCustomApprovalStatus_shouldPassIt() {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("Test Artist").build();
        ArtistDto expected = ArtistDto.builder().id(1L).name("Test Artist").build();

        when(artistService.saveArtist(request, Origin.ETL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        ArtistDto result = controller.createArtist(request, MasterApprovalStatus.APPROVED);

        assertEquals(expected, result);
        verify(artistService).saveArtist(request, Origin.ETL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void bindArtistToExisting_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 123L;
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder().masterId(1L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Artist");

        when(artistService.bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.bindArtistToExisting(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(artistService).bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    @Test
    void createAndBindArtist_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 123L;
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder().entityName("Artist").build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Artist");

        when(artistService.createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.createAndBindArtist(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(artistService).createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    // --- Albums ---

    @Test
    void createAlbum_shouldPassEtlOriginAndDefaultApprovalStatus() {
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder().name("Test Album").primaryArtistId(1L).build();
        AlbumDto expected = AlbumDto.builder().id(1L).name("Test Album").build();

        when(albumService.saveAlbum(request, Origin.ETL, MasterApprovalStatus.NEW)).thenReturn(expected);

        AlbumDto result = controller.createAlbum(request, MasterApprovalStatus.NEW);

        assertEquals(expected, result);
        verify(albumService).saveAlbum(request, Origin.ETL, MasterApprovalStatus.NEW);
    }

    @Test
    void bindAlbumToExisting_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 456L;
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(1L).masterPrimaryArtistId(2L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Album");

        when(albumService.bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.bindAlbumToExisting(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(albumService).bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    @Test
    void createAndBindAlbum_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 456L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Album").masterPrimaryArtistId(2L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Album");

        when(albumService.createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.createAndBindAlbum(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(albumService).createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    @Test
    void createAndBindAlbumWithTracks_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 456L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("Album").masterPrimaryArtistId(2L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Album");

        when(albumService.createAndBindWithTracks(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.createAndBindAlbumWithTracks(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(albumService).createAndBindWithTracks(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    // --- Tracks ---

    @Test
    void createTrack_shouldPassEtlOriginAndDefaultApprovalStatus() {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder().name("Test Track").primaryArtistId(1L).build();
        TrackDto expected = TrackDto.builder().id(1L).name("Test Track").build();

        when(trackService.saveTrack(request, Origin.ETL, MasterApprovalStatus.NEW)).thenReturn(expected);

        TrackDto result = controller.createTrack(request, MasterApprovalStatus.NEW);

        assertEquals(expected, result);
        verify(trackService).saveTrack(request, Origin.ETL, MasterApprovalStatus.NEW);
    }

    @Test
    void bindTrackToExisting_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 789L;
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(1L).masterPrimaryArtistId(2L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Track");

        when(trackService.bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.bindTrackToExisting(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(trackService).bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    @Test
    void createAndBindTrack_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 789L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Track").masterPrimaryArtistId(2L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Track");

        when(trackService.createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.createAndBindTrack(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(trackService).createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    // --- Categories ---

    @Test
    void createCategory_shouldPassEtlOriginAndDefaultApprovalStatus() {
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder().name("Test Category").build();
        CategoryDto expected = CategoryDto.builder().id(1L).name("Test Category").build();

        when(categoryService.saveCategory(request, Origin.ETL, MasterApprovalStatus.NEW)).thenReturn(expected);

        CategoryDto result = controller.createCategory(request, MasterApprovalStatus.NEW);

        assertEquals(expected, result);
        verify(categoryService).saveCategory(request, Origin.ETL, MasterApprovalStatus.NEW);
    }

    @Test
    void bindCategoryToExisting_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 101L;
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder().masterId(1L).build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Category");

        when(categoryService.bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.bindCategoryToExisting(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(categoryService).bindToExisting(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    @Test
    void createAndBindCategory_shouldPassEtlOriginAndDefaultPendingStatus() {
        DataSource dataSource = DataSource.SPOTIFY;
        Long externalId = 101L;
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder().entityName("Category").build();
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Category");

        when(categoryService.createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING))
            .thenReturn(projection);

        BoundEntityProjection result = controller.createAndBindCategory(dataSource, externalId, request, MasterApprovalStatus.PENDING);

        assertEquals(projection, result);
        verify(categoryService).createAndBind(dataSource, externalId, request, Origin.ETL, MasterApprovalStatus.PENDING);
    }

    // --- Exception pass-through ---

    @Test
    void createArtist_whenExceptionThrown_shouldPassThrough() {
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder().name("Test").build();
        RuntimeException expected = new RuntimeException("Service error");

        when(artistService.saveArtist(request, Origin.ETL, MasterApprovalStatus.NEW)).thenThrow(expected);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            controller.createArtist(request, MasterApprovalStatus.NEW)
        );

        assertSame(expected, exception);
    }

    @Test
    void createAlbum_whenExceptionThrown_shouldPassThrough() {
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder().name("Test").primaryArtistId(1L).build();
        RuntimeException expected = new RuntimeException("Service error");

        when(albumService.saveAlbum(request, Origin.ETL, MasterApprovalStatus.NEW)).thenThrow(expected);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            controller.createAlbum(request, MasterApprovalStatus.NEW)
        );

        assertSame(expected, exception);
    }
}
