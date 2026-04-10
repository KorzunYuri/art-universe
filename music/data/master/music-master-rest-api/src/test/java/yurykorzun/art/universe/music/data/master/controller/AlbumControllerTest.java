package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ExternalAlbumWithTracksCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.TrackReorderItemDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackReorderRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumControllerTest {

    @Mock
    private AlbumService albumService;

    @Mock
    private BindingService bindingService;

    @InjectMocks
    private AlbumController albumController;

    @Test
    void findBoundAlbums_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Album"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = albumController.findBoundAlbums(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }

    @Test
    void findBoundAlbums_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        RuntimeException expectedException = new RuntimeException("Test exception");
        
        when(albumService.findBoundAlbums(dataSource, externalIds))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            albumController.findBoundAlbums(dataSource, externalIds)
        );
        
        assertSame(expectedException, exception);
        verify(albumService).findBoundAlbums(dataSource, externalIds);
    }
    
    @Test
    void findBoundAlbums_withNoResults_shouldReturnEmptyList() {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(albumService.findBoundAlbums(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When
        List<BoundEntityProjection> result = albumController.findBoundAlbums(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(emptyList, result);
        verify(albumService).findBoundAlbums(eq(dataSource), any());
    }
    
    @Test
    void lookupAlbums_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "computer";
        DataSource dataSource = DataSource.LASTFM;
        Long masterArtistId = 123L;
        Long externalArtistId = null;
        Integer limit = null;
        
        LookupResultDTO album1 = new LookupResultDTO(1L, "Radiohead - OK Computer");
        LookupResultDTO album2 = new LookupResultDTO(2L, "Kraftwerk - Computer World");
        List<LookupResultDTO> expectedAlbums = List.of(album1, album2);
        
        when(albumService.lookupAlbums(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedAlbums);
            
        // When
        List<LookupResultDTO> result = albumController.lookupAlbums(searchTerm, dataSource, masterArtistId, externalArtistId, limit);
            
        // Then
        assertEquals(expectedAlbums, result);
        verify(albumService).lookupAlbums(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupAlbums_withAllParameters_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "computer";
        DataSource dataSource = DataSource.LASTFM;
        Long masterArtistId = 123L;
        Long externalArtistId = 456L;
        Integer limit = 10;
        
        LookupResultDTO album1 = new LookupResultDTO(1L, "Radiohead - OK Computer");
        List<LookupResultDTO> expectedAlbums = List.of(album1);
        
        when(albumService.lookupAlbums(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedAlbums);
            
        // When
        List<LookupResultDTO> result = albumController.lookupAlbums(searchTerm, dataSource, masterArtistId, externalArtistId, limit);
            
        // Then
        assertEquals(expectedAlbums, result);
        
        // Verify that the request was created with the correct parameters
        verify(albumService).lookupAlbums(argThat(request -> 
            searchTerm.equals(request.getSearch()) &&
            masterArtistId.equals(request.getMasterArtistId()) &&
            externalArtistId.equals(request.getExternalArtistId()) &&
            limit.equals(request.getLimit())
        ));
    }
    
    @Test
    void lookupAlbums_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String searchTerm = "computer";
        DataSource dataSource = DataSource.LASTFM;
        Long masterArtistId = 123L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(albumService.lookupAlbums(any(ArtistRelatedLookupRequestDTO.class)))
            .thenThrow(expectedException);
            
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            albumController.lookupAlbums(searchTerm, dataSource, masterArtistId, null, null)
        );
        
        assertSame(expectedException, exception);
        verify(albumService).lookupAlbums(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void batchLookupAlbums_shouldReturnBatchLookupResponseDTO() {
        // Given
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
            .search("computer")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
            .search("kid")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("computer", List.of(
            new LookupResultDTO(1L, "Radiohead - OK Computer")
        ));
        resultMap.put("kid", List.of(
            new LookupResultDTO(2L, "Radiohead - Kid A")
        ));
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(albumService.batchLookupAlbums(batchRequest)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = albumController.batchLookupAlbums(batchRequest);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(albumService).batchLookupAlbums(batchRequest);
    }
    
    @Test
    void batchLookupAlbums_whenExceptionThrown_shouldPassThroughException() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("computer")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();

        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(List.of(request))
            .limit(10)
            .build();

        RuntimeException expectedException = new RuntimeException("Test error");

        when(albumService.batchLookupAlbums(batchRequest))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            albumController.batchLookupAlbums(batchRequest)
        );

        assertSame(expectedException, exception);
        verify(albumService).batchLookupAlbums(batchRequest);
    }

    // --- saveAlbum ---

    @Test
    void saveAlbum_shouldCallService_andReturnAlbumDto() {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .build();
        AlbumDto expected = AlbumDto.builder().id(1L).name("OK Computer").primaryArtistId(10L).build();

        when(albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        // When
        AlbumDto result = albumController.saveAlbum(request);

        // Then
        assertEquals(expected, result);
        verify(albumService).saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void saveAlbum_withExistingId_shouldCallService_andReturnUpdatedDto() {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .id(5L)
            .name("OK Computer (Remastered)")
            .build();
        AlbumDto expected = AlbumDto.builder().id(5L).name("OK Computer (Remastered)").primaryArtistId(10L).build();

        when(albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        // When
        AlbumDto result = albumController.saveAlbum(request);

        // Then
        assertEquals(expected, result);
        verify(albumService).saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    // --- deleteAlbum ---

    @Test
    void deleteAlbum_whenExists_shouldReturnTrue() {
        // Given
        Long albumId = 1L;
        when(albumService.deleteAlbum(albumId)).thenReturn(true);

        // When
        boolean result = albumController.deleteAlbum(albumId);

        // Then
        assertTrue(result);
        verify(albumService).deleteAlbum(albumId);
    }

    @Test
    void deleteAlbum_whenNotExists_shouldThrowCustomEntityNotFoundException() {
        // Given
        Long albumId = 999L;
        when(albumService.deleteAlbum(albumId)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumController.deleteAlbum(albumId));
        verify(albumService).deleteAlbum(albumId);
    }

    // --- saveAlbumWithTracks ---

    @Test
    void saveAlbumWithTracks_shouldCallService_andReturnAlbumDto() {
        // Given
        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("OK Computer")
            .primaryArtistId(10L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(1L).trackOrder(1).build(),
                AlbumTrackItemDTO.builder().trackId(2L).trackOrder(2).build()
            ))
            .build();
        AlbumDto expected = AlbumDto.builder().id(1L).name("OK Computer").primaryArtistId(10L).build();

        when(albumService.saveAlbumWithTracks(request)).thenReturn(expected);

        // When
        AlbumDto result = albumController.saveAlbumWithTracks(request);

        // Then
        assertEquals(expected, result);
        verify(albumService).saveAlbumWithTracks(request);
    }

    // --- createAndBind ---

    @Test
    void createAndBind_shouldCallService_andReturnBoundProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 500L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("OK Computer")
            .masterPrimaryArtistId(100L)
            .build();
        BoundEntityProjection expected = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        // When
        BoundEntityProjection result = albumController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expected, result);
        verify(albumService).createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void createAndBind_whenServiceThrows_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 500L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("OK Computer")
            .masterPrimaryArtistId(100L)
            .build();
        RuntimeException expectedException = new RuntimeException("Artist not bound");

        when(albumService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            albumController.createAndBind(dataSource, externalId, request));

        assertSame(expectedException, exception);
        verify(albumService).createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    // --- createAndBindWithTracks ---

    @Test
    void createAndBindWithTracks_shouldCallService_andReturnBoundProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(10L).trackName("Airbag").trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(20L).masterTrackId(200L).trackOrder(2).build()
            ))
            .build();
        BoundEntityProjection expected = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(dataSource, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        // When
        BoundEntityProjection result = albumController.createAndBindWithTracks(dataSource, externalAlbumId, request);

        // Then
        assertEquals(expected, result);
        verify(albumService).createAndBindWithTracks(dataSource, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void createAndBindWithTracks_withPerTrackArtistOverride_shouldPassOverrideToService() {
        // Given — second track has a per-track masterPrimaryArtistId overriding the album-level artist
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        Long albumArtistId = 100L;
        Long trackArtistOverrideId = 200L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(albumArtistId)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(10L).trackName("Airbag").trackOrder(1).build(),
                ExternalAlbumTrackItemDTO.builder()
                    .externalTrackId(20L).trackName("Paranoid Android").trackOrder(2)
                    .masterPrimaryArtistId(trackArtistOverrideId)
                    .build()
            ))
            .build();
        BoundEntityProjection expected = new TestBoundEntityProjectionImpl(externalAlbumId, dataSource, 1L, "OK Computer");

        when(albumService.createAndBindWithTracks(dataSource, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        // When
        BoundEntityProjection result = albumController.createAndBindWithTracks(dataSource, externalAlbumId, request);

        // Then
        assertEquals(expected, result);
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
    void createAndBindWithTracks_whenServiceThrows_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalAlbumId = 500L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .albumName("OK Computer")
            .masterPrimaryArtistId(100L)
            .tracks(List.of(
                ExternalAlbumTrackItemDTO.builder().externalTrackId(10L).trackName("Airbag").trackOrder(1).build()
            ))
            .build();
        RuntimeException expectedException = new RuntimeException("Artist not bound");

        when(albumService.createAndBindWithTracks(dataSource, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            albumController.createAndBindWithTracks(dataSource, externalAlbumId, request));

        assertSame(expectedException, exception);
        verify(albumService).createAndBindWithTracks(dataSource, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    // --- findAlbums ---

    @Test
    void findAlbums_shouldDelegateToService() {
        String search = "abbey";
        Long categoryId = 5L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<AlbumDto> expected = new PageImpl<>(List.of(
            AlbumDto.builder().id(1L).name("Abbey Road").primaryArtistId(10L).build()
        ));
        when(albumService.findAlbums(search, categoryId, pageable)).thenReturn(expected);

        Page<AlbumDto> result = albumController.findAlbums(search, categoryId, pageable);

        assertEquals(expected, result);
        verify(albumService).findAlbums(search, categoryId, pageable);
    }

    // --- findAlbumsWithCategories ---

    @Test
    void findAlbumsWithCategories_shouldDelegateToService() {
        String search = "abbey";
        Pageable pageable = PageRequest.of(0, 10);
        Page<AlbumWithCategoriesDto> expected = new PageImpl<>(List.of());
        when(albumService.findAlbumsWithCategories(search, null, pageable)).thenReturn(expected);

        Page<AlbumWithCategoriesDto> result = albumController.findAlbumsWithCategories(search, null, pageable);

        assertEquals(expected, result);
    }

    // --- getAlbum ---

    @Test
    void getAlbum_shouldDelegateToService() {
        AlbumDto expected = AlbumDto.builder().id(1L).name("Abbey Road").primaryArtistId(10L).build();
        when(albumService.getAlbum(1L)).thenReturn(expected);

        AlbumDto result = albumController.getAlbum(1L);

        assertEquals(expected, result);
    }

    // --- getAlbumWithCategories ---

    @Test
    void getAlbumWithCategories_shouldDelegateToService() {
        AlbumWithCategoriesDto expected = AlbumWithCategoriesDto.builder()
            .id(1L).name("Abbey Road").primaryArtistId(10L).categories(List.of()).build();
        when(albumService.getAlbumWithCategories(1L)).thenReturn(expected);

        AlbumWithCategoriesDto result = albumController.getAlbumWithCategories(1L);

        assertEquals(expected, result);
    }

    // --- bindToCategory ---

    @Test
    void bindToCategory_shouldDelegateToService() {
        albumController.bindToCategory(1L, 2L);

        verify(albumService).bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    // --- unbindFromCategory ---

    @Test
    void unbindFromCategory_shouldDelegateToService() {
        albumController.unbindFromCategory(1L, 2L);

        verify(albumService).unbindFromCategory(1L, 2L);
    }

    // --- bindToExisting ---

    @Test
    void bindToExisting_shouldDelegateToService() {
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 500L;
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(1L).masterPrimaryArtistId(100L).build();
        BoundEntityProjection expected = new TestBoundEntityProjectionImpl(externalId, dataSource, 1L, "Test");
        when(albumService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        BoundEntityProjection result = albumController.bindToExisting(dataSource, externalId, request);

        assertEquals(expected, result);
    }

    // --- unbindAlbum ---

    @Test
    void unbindAlbum_shouldDelegateToService() {
        DataSource dataSource = DataSource.LASTFM;
        when(albumService.unbindAlbum(dataSource, 100L)).thenReturn(true);

        boolean result = albumController.unbindAlbum(dataSource, 100L);

        assertTrue(result);
    }

    // --- copyTracklist ---

    @Test
    void copyTracklist_shouldReturnCopiedCount() {
        when(albumService.copyTracklist(1L, 2L)).thenReturn(5);

        Map<String, Integer> result = albumController.copyTracklist(1L, 2L);

        assertEquals(5, result.get("copiedCount"));
        verify(albumService).copyTracklist(1L, 2L);
    }

    // --- reorderTracks ---

    @Test
    void reorderTracks_shouldDelegateToService() {
        Long albumId = 1L;
        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(10L).newOrder(1).build(),
            TrackReorderItemDTO.builder().albumTrackId(20L).newOrder(2).build()
        );
        TrackReorderRequestDTO request = TrackReorderRequestDTO.builder().items(items).build();

        albumController.reorderTracks(albumId, request);

        verify(albumService).reorderTracks(albumId, items);
    }

    // --- batchUnbindAlbums ---

    @Test
    void batchUnbindAlbums_shouldDelegateToBindingService() {
        DataSource dataSource = DataSource.LASTFM;
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(List.of(1L, 2L, 3L)).build();
        BatchUnbindResponseDTO expected = BatchUnbindResponseDTO.builder()
            .successfullyUnbound(List.of(1L, 2L))
            .notFound(List.of(3L))
            .totalProcessed(3).successCount(2).notFoundCount(1)
            .build();
        when(bindingService.batchUnbind(MasterEntityType.ALBUM, dataSource, request)).thenReturn(expected);

        BatchUnbindResponseDTO result = albumController.batchUnbindAlbums(dataSource, request);

        assertEquals(expected, result);
        verify(bindingService).batchUnbind(MasterEntityType.ALBUM, dataSource, request);
    }
}
