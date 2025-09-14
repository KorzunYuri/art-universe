package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.ArtistSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.ArtistService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistControllerTest {

    @Mock
    private ArtistService artistService;

    @Mock
    private BindingService bindingService;

    @InjectMocks
    private ArtistController artistController;

    @Test
    void findBoundArtists_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Test Artist"
        );

        List<BoundEntityProjection> expectedBindings = List.of(projection);
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenReturn(expectedBindings);

        // When
        List<BoundEntityProjection> result = artistController.findBoundArtists(dataSource, externalIds);

        // Then
        assertEquals(expectedBindings, result);
        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void findBoundArtists_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(artistService.findBoundArtists(dataSource, externalIds))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.findBoundArtists(dataSource, externalIds)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).findBoundArtists(dataSource, externalIds);
    }
    
    @Test
    void lookupArtists_shouldReturnListOfLookupResults() {
        // Given
        String search = "radio";
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(search, null);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void lookupArtists_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String search = "radio";
        Integer limit = 5;
        LookupResultDTO artist1 = new LookupResultDTO(1L, "Radiohead");
        LookupResultDTO artist2 = new LookupResultDTO(2L, "Radio Moscow");
        List<LookupResultDTO> expectedArtists = List.of(artist1, artist2);
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class))).thenReturn(expectedArtists);
            
        // When
        List<LookupResultDTO> result = artistController.lookupArtists(search, limit);
            
        // Then
        assertEquals(expectedArtists, result);
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void lookupArtists_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String search = "radio";
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(artistService.lookupArtists(any(LookupRequestDTO.class)))
            .thenThrow(expectedException);
            
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.lookupArtists(search, null)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).lookupArtists(any(LookupRequestDTO.class));
    }
    
    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Radiohead"
        );
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = artistController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void bindToExisting_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        when(artistService.bindToExisting(dataSource, externalId, request))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.bindToExisting(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).bindToExisting(dataSource, externalId, request);
    }
    
    @Test
    void findArtists_shouldReturnPageOfArtists() {
        // Given
        String search = "radio";
        Pageable pageable = PageRequest.of(0, 10);
        
        ArtistDto artist1 = ArtistDto.builder().id(1L).name("Radiohead").build();
        ArtistDto artist2 = ArtistDto.builder().id(2L).name("Radio Moscow").build();
        List<ArtistDto> artists = List.of(artist1, artist2);
        Page<ArtistDto> expectedPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistService.findArtists(search, pageable)).thenReturn(expectedPage);

        // When
        Page<ArtistDto> result = artistController.findArtists(search, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(artistService).findArtists(search, pageable);
    }

    @Test
    void findArtists_withNullSearch_shouldReturnAllArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        ArtistDto artist = ArtistDto.builder().id(1L).name("Test Artist").build();
        Page<ArtistDto> expectedPage = new PageImpl<>(List.of(artist), pageable, 1);
        
        when(artistService.findArtists(null, pageable)).thenReturn(expectedPage);

        // When
        Page<ArtistDto> result = artistController.findArtists(null, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(artistService).findArtists(null, pageable);
    }

    @Test
    void findArtists_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String search = "radio";
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(artistService.findArtists(search, pageable)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.findArtists(search, pageable)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).findArtists(search, pageable);
    }

    @Test
    void saveArtist_shouldReturnArtistDto() {
        // Given
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .name("New Artist")
            .build();
        
        ArtistDto savedArtist = ArtistDto.builder()
            .id(1L)
            .name("New Artist")
            .build();
        
        when(artistService.saveArtist(request)).thenReturn(savedArtist);

        // When
        ArtistDto result = artistController.saveArtist(request);

        // Then
        assertEquals(savedArtist, result);
        verify(artistService).saveArtist(request);
    }

    @Test
    void saveArtist_whenExceptionThrown_shouldPassThroughException() {
        // Given
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .name("New Artist")
            .build();
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(artistService.saveArtist(request)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.saveArtist(request)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).saveArtist(request);
    }

    @Test
    void deleteArtist_whenFound_shouldReturnTrue() {
        // Given
        Long id = 1L;
        
        when(artistService.deleteArtist(id)).thenReturn(true);

        // When
        boolean result = artistController.deleteArtist(id);

        // Then
        assertTrue(result);
        verify(artistService).deleteArtist(id);
    }

    @Test
    void deleteArtist_whenNotFound_shouldThrowEntityNotFoundException() {
        // Given
        Long id = 1L;
        
        when(artistService.deleteArtist(id)).thenReturn(false);

        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            artistController.deleteArtist(id)
        );
        
        assertEquals("Artist not found with id: " + id, exception.getMessage());
        verify(artistService).deleteArtist(id);
    }

    @Test
    void deleteArtist_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long id = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(artistService.deleteArtist(id)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.deleteArtist(id)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).deleteArtist(id);
    }
    
    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "New Artist";
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, artistName
        );
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = artistController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(artistService).createAndBind(dataSource, externalId, request);
    }
    
    @Test
    void createAndBind_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        RuntimeException expectedException = new RuntimeException("Test error");
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        when(artistService.createAndBind(dataSource, externalId, request))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.createAndBind(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).createAndBind(dataSource, externalId, request);
    }
    
    @Test
    void unbindArtist_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(artistService.unbindArtist(dataSource, externalId)).thenReturn(true);

        // When
        boolean result = artistController.unbindArtist(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(artistService).unbindArtist(dataSource, externalId);
    }
    
    @Test
    void unbindArtist_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(artistService.unbindArtist(dataSource, externalId))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.unbindArtist(dataSource, externalId)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).unbindArtist(dataSource, externalId);
    }
    
    @Test
    void batchLookupArtists_shouldReturnBatchLookupResponseDTO() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        Integer limit = 10;
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(searchTerms))
            .limit(limit)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("radio", List.of(
            new LookupResultDTO(1L, "Radiohead"),
            new LookupResultDTO(2L, "Radio Moscow")
        ));
        resultMap.put("queen", List.of(
            new LookupResultDTO(3L, "Queen")
        ));
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(artistService.batchLookupArtists(request)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = artistController.batchLookupArtists(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(artistService).batchLookupArtists(request);
    }
    
    @Test
    void batchLookupArtists_whenExceptionThrown_shouldPassThroughException() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        Integer limit = 10;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(searchTerms))
            .limit(limit)
            .build();
        
        when(artistService.batchLookupArtists(request))
            .thenThrow(expectedException);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            artistController.batchLookupArtists(request)
        );
        
        assertSame(expectedException, exception);
        verify(artistService).batchLookupArtists(request);
    }
    
    /**
     * Helper method to convert a list of search terms to a list of LookupRequestDTO
     */
    private List<LookupRequestDTO> createLookupRequests(List<String> searchTerms) {
        return searchTerms.stream()
            .map(term -> LookupRequestDTO.builder().search(term).build())
            .collect(Collectors.toList());
    }
}
