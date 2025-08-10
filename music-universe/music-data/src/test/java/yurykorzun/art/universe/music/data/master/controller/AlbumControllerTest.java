package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.service.AlbumService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
}
