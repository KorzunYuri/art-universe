package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
import yurykorzun.art.universe.music.data.master.service.BindingService;
import yurykorzun.art.universe.music.data.master.service.TrackService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.TrackDto;
import yurykorzun.art.universe.music.data.master.dto.TrackSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackControllerTest {

    @Mock
    private TrackService trackService;

    @Mock
    private BindingService bindingService;

    @InjectMocks
    private TrackController trackController;

    @Test
    void findBoundTracks_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            101L, dataSource, 201L, "Test Track"
        );
        List<BoundEntityProjection> mockBindings = List.of(projection);
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = trackController.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(trackService).findBoundTracks(dataSource, externalIds);
    }

    @Test
    void findBoundTracks_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L);
        RuntimeException expectedException = new RuntimeException("Test exception");
        
        when(trackService.findBoundTracks(dataSource, externalIds))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.findBoundTracks(dataSource, externalIds)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).findBoundTracks(dataSource, externalIds);
    }
    
    @Test
    void findBoundTracks_withNoResults_shouldReturnEmptyList() {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        
        when(trackService.findBoundTracks(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When
        List<BoundEntityProjection> result = trackController.findBoundTracks(dataSource, List.of(999L, 888L));

        // Then
        assertEquals(emptyList, result);
        verify(trackService).findBoundTracks(eq(dataSource), any());
    }

    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 123L;
        Long primaryArtistId = 456L;
        
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .masterPrimaryArtistId(primaryArtistId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Test Track"
        );
        
        when(trackService.bindToExisting(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))
        )
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).bindToExisting(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 123L;
        Long primaryArtistId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .masterPrimaryArtistId(primaryArtistId)
            .build();
        
        when(trackService.bindToExisting(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))
        )
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.bindToExisting(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).bindToExisting(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityBindToExistingRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }
    
    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long primaryArtistId = 100L;
        
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .masterPrimaryArtistId(primaryArtistId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, trackName
        );
        
        when(trackService.createAndBind(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))
        )
            .thenReturn(projection);

        // When
        BoundEntityProjection result = trackController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(trackService).createAndBind(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String trackName = "Test Track";
        Long primaryArtistId = 100L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .masterPrimaryArtistId(primaryArtistId)
            .build();
        
        when(trackService.createAndBind(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED))
        )
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.createAndBind(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).createAndBind(
            eq(dataSource), eq(externalId), any(ArtistRelatedEntityCreateAndBindRequestDTO.class),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }
    
    @Test
    void unbindTrack_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(trackService.unbindTrack(dataSource, externalId)).thenReturn(true);

        // When
        boolean result = trackController.unbindTrack(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(trackService).unbindTrack(dataSource, externalId);
    }
    
    @Test
    void unbindTrack_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(trackService.unbindTrack(dataSource, externalId))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.unbindTrack(dataSource, externalId)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).unbindTrack(dataSource, externalId);
    }
    
    @Test
    void lookupTracks_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        DataSource dataSource = DataSource.LASTFM;
        Long artistId = 123L;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, dataSource, artistId, null, null);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        DataSource dataSource = DataSource.LASTFM;
        Long artistId = 123L;
        Integer limit = 5;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, dataSource, artistId, null, limit);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_withoutArtistId_shouldReturnListOfLookupResults() {
        // Given
        String searchTerm = "paranoid";
        DataSource dataSource = DataSource.LASTFM;
        Long artistId = null;
        
        LookupResultDTO track1 = new LookupResultDTO(1L, "Radiohead - Paranoid Android");
        LookupResultDTO track2 = new LookupResultDTO(2L, "Black Sabbath - Paranoid");
        List<LookupResultDTO> expectedTracks = List.of(track1, track2);
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenReturn(expectedTracks);
            
        // When
        List<LookupResultDTO> result = trackController.lookupTracks(searchTerm, dataSource, artistId, null, null);
            
        // Then
        assertEquals(expectedTracks, result);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void lookupTracks_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String searchTerm = "paranoid";
        DataSource dataSource = DataSource.LASTFM;
        Long artistId = 123L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(trackService.lookupTracks(any(ArtistRelatedLookupRequestDTO.class)))
            .thenThrow(expectedException);
            
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.lookupTracks(searchTerm, dataSource, artistId, null, null)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).lookupTracks(any(ArtistRelatedLookupRequestDTO.class));
    }
    
    @Test
    void batchLookupTracks_shouldReturnBatchLookupResponseDTO() {
        // Given
        List<String> searchTerms = List.of("paranoid", "karma");
        Long artistId = 123L;
        Integer limit = 10;
        
        ArtistRelatedBatchLookupRequestDTO request = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(createArtistRelatedLookupRequests(searchTerms, artistId))
            .limit(limit)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("paranoid", List.of(
            new LookupResultDTO(1L, "Radiohead - Paranoid Android"),
            new LookupResultDTO(2L, "Black Sabbath - Paranoid")
        ));
        resultMap.put("karma", List.of(
            new LookupResultDTO(3L, "Radiohead - Karma Police")
        ));
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(trackService.batchLookupTracks(request)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = trackController.batchLookupTracks(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(trackService).batchLookupTracks(request);
    }
    
    @Test
    void batchLookupTracks_whenExceptionThrown_shouldPassThroughException() {
        // Given
        List<String> searchTerms = List.of("paranoid", "karma");
        Long artistId = 123L;
        Integer limit = 10;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        ArtistRelatedBatchLookupRequestDTO request = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(createArtistRelatedLookupRequests(searchTerms, artistId))
            .limit(limit)
            .build();
        
        when(trackService.batchLookupTracks(request))
            .thenThrow(expectedException);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            trackController.batchLookupTracks(request)
        );
        
        assertSame(expectedException, exception);
        verify(trackService).batchLookupTracks(request);
    }
    
    // --- findTracks ---

    @Test
    void findTracks_shouldDelegateToService() {
        String search = "paranoid";
        Long categoryId = 5L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrackDto> expected = new PageImpl<>(List.of(
            TrackDto.builder().id(1L).name("Paranoid Android").primaryArtistId(10L).build()
        ));
        when(trackService.findTracks(search, categoryId, pageable)).thenReturn(expected);

        Page<TrackDto> result = trackController.findTracks(search, categoryId, pageable);

        assertEquals(expected, result);
        verify(trackService).findTracks(search, categoryId, pageable);
    }

    // --- findTracksWithCategories ---

    @Test
    void findTracksWithCategories_shouldDelegateToService() {
        String search = "paranoid";
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrackWithCategoriesDto> expected = new PageImpl<>(List.of());
        when(trackService.findTracksWithCategories(search, null, pageable)).thenReturn(expected);

        Page<TrackWithCategoriesDto> result = trackController.findTracksWithCategories(search, null, pageable);

        assertEquals(expected, result);
    }

    // --- getTrack ---

    @Test
    void getTrack_shouldDelegateToService() {
        TrackDto expected = TrackDto.builder().id(1L).name("Paranoid Android").primaryArtistId(10L).build();
        when(trackService.getTrack(1L)).thenReturn(expected);

        TrackDto result = trackController.getTrack(1L);

        assertEquals(expected, result);
    }

    // --- getTrackWithCategories ---

    @Test
    void getTrackWithCategories_shouldDelegateToService() {
        TrackWithCategoriesDto expected = TrackWithCategoriesDto.builder()
            .id(1L).name("Paranoid Android").primaryArtistId(10L).categories(List.of()).build();
        when(trackService.getTrackWithCategories(1L)).thenReturn(expected);

        TrackWithCategoriesDto result = trackController.getTrackWithCategories(1L);

        assertEquals(expected, result);
    }

    // --- saveTrack ---

    @Test
    void saveTrack_shouldDelegateToService() {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder()
            .name("New Track").primaryArtistId(10L).build();
        TrackDto expected = TrackDto.builder().id(1L).name("New Track").primaryArtistId(10L).build();
        when(trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(expected);

        TrackDto result = trackController.saveTrack(request);

        assertEquals(expected, result);
    }

    // --- deleteTrack ---

    @Test
    void deleteTrack_whenExists_shouldReturnTrue() {
        when(trackService.deleteTrack(1L)).thenReturn(true);

        boolean result = trackController.deleteTrack(1L);

        assertTrue(result);
    }

    @Test
    void deleteTrack_whenNotExists_shouldThrow() {
        when(trackService.deleteTrack(999L)).thenReturn(false);

        assertThrows(CustomEntityNotFoundException.class, () -> trackController.deleteTrack(999L));
    }

    // --- bindToCategory ---

    @Test
    void bindToCategory_shouldDelegateToService() {
        trackController.bindToCategory(1L, 2L);

        verify(trackService).bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    // --- unbindFromCategory ---

    @Test
    void unbindFromCategory_shouldDelegateToService() {
        trackController.unbindFromCategory(1L, 2L);

        verify(trackService).unbindFromCategory(1L, 2L);
    }

    // --- batchUnbindTracks ---

    @Test
    void batchUnbindTracks_shouldDelegateToBindingService() {
        DataSource dataSource = DataSource.LASTFM;
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(List.of(1L, 2L)).build();
        BatchUnbindResponseDTO expected = BatchUnbindResponseDTO.builder()
            .successfullyUnbound(List.of(1L))
            .notFound(List.of(2L))
            .totalProcessed(2).successCount(1).notFoundCount(1)
            .build();
        when(bindingService.batchUnbind(MasterEntityType.TRACK, dataSource, request)).thenReturn(expected);

        BatchUnbindResponseDTO result = trackController.batchUnbindTracks(dataSource, request);

        assertEquals(expected, result);
        verify(bindingService).batchUnbind(MasterEntityType.TRACK, dataSource, request);
    }

    /**
     * Helper method to convert a list of search terms to a list of ArtistRelatedLookupRequestDTO
     */
    private List<ArtistRelatedLookupRequestDTO> createArtistRelatedLookupRequests(List<String> searchTerms, Long artistId) {
        return searchTerms.stream()
            .map(term -> ArtistRelatedLookupRequestDTO.builder()
                .search(term)
                .masterArtistId(artistId)
                .dataSource(DataSource.LASTFM)
                .build())
            .collect(Collectors.toList());
    }
}
