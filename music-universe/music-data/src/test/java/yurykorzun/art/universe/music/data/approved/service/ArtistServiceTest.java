package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.ArtistBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.Artist;
import yurykorzun.art.universe.music.data.approved.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistBindingRepository artistBindingRepository;

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Test
    void findBoundArtists_shouldReturnListOfBoundArtists() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Test Artist"
        );
        List<BoundEntityProjection> expectedResult = List.of(projection);
        
        when(artistBindingRepository.findBoundArtistsForDataSource(dataSource, externalIds))
            .thenReturn(expectedResult);

        // When
        List<BoundEntityProjection> result = artistService.findBoundArtists(dataSource, externalIds);

        // Then
        assertEquals(expectedResult, result);
        verify(artistBindingRepository).findBoundArtistsForDataSource(dataSource, externalIds);
    }
    @Test
    void findArtist_shouldReturnSingleBoundArtist() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, "Test Artist"
        );
        
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = artistService.findArtist(dataSource, externalId);

        // Then
        assertEquals(projection, result);
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }

    @Test
    void findArtist_whenNotFound_shouldReturnNull() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(null);

        // When
        BoundEntityProjection result = artistService.findArtist(dataSource, externalId);

        // Then
        assertNull(result);
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }
    @Test
    void bindArtist_whenArtistExists_shouldCreateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        
        Artist existingArtist = Artist.builder()
            .id(101L)
            .name(artistName)
            .build();
        
        ArtistBindingRequestDTO request = ArtistBindingRequestDTO.builder()
            .name(artistName)
            .build();
        
        ArtistBinding binding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(existingArtist.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, existingArtist.getId(), artistName
        );
        
        when(artistRepository.findByName(artistName)).thenReturn(Optional.of(existingArtist));
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(artistBindingRepository.save(any(ArtistBinding.class))).thenReturn(binding);
        when(artistBindingRepository.findBoundArtistsForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = artistService.bindArtist(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistRepository).findByName(artistName);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(any(ArtistBinding.class));
        verify(artistBindingRepository).findBoundArtistsForDataSource(dataSource, List.of(externalId));
    }
    
    @Test
    void bindArtist_whenArtistDoesNotExist_shouldCreateArtistAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "New Artist";
        
        Artist newArtist = Artist.builder()
            .id(101L)
            .name(artistName)
            .build();
        
        ArtistBindingRequestDTO request = ArtistBindingRequestDTO.builder()
            .name(artistName)
            .build();
        
        ArtistBinding binding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(newArtist.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newArtist.getId(), artistName
        );
        
        when(artistRepository.findByName(artistName)).thenReturn(Optional.empty());
        when(artistRepository.save(any(Artist.class))).thenReturn(newArtist);
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(artistBindingRepository.save(any(ArtistBinding.class))).thenReturn(binding);
        when(artistBindingRepository.findBoundArtistsForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = artistService.bindArtist(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistRepository).findByName(artistName);
        verify(artistRepository).save(any(Artist.class));
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(any(ArtistBinding.class));
        verify(artistBindingRepository).findBoundArtistsForDataSource(dataSource, List.of(externalId));
    }
    
    @Test
    void bindArtist_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Test Artist";
        
        Artist existingArtist = Artist.builder()
            .id(101L)
            .name(artistName)
            .build();
        
        Artist oldArtist = Artist.builder()
            .id(102L)
            .name("Old Artist")
            .build();
        
        ArtistBinding existingBinding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(oldArtist.getId())
            .build();
        
        ArtistBindingRequestDTO request = ArtistBindingRequestDTO.builder()
            .name(artistName)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, existingArtist.getId(), artistName
        );
        
        when(artistRepository.findByName(artistName)).thenReturn(Optional.of(existingArtist));
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(artistBindingRepository.save(any(ArtistBinding.class))).thenReturn(existingBinding);
        when(artistBindingRepository.findBoundArtistsForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = artistService.bindArtist(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistRepository).findByName(artistName);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(existingBinding);
        verify(artistBindingRepository).findBoundArtistsForDataSource(dataSource, List.of(externalId));
    }
    
    @Test
    void unbindArtist_whenBindingExists_shouldDeleteBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        ArtistBinding existingBinding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(101L)
            .build();
        
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        doNothing().when(artistBindingRepository).delete(existingBinding);

        // When
        boolean result = artistService.unbindArtist(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).delete(existingBinding);
    }
    
    @Test
    void unbindArtist_whenBindingDoesNotExist_shouldReturnFalse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());

        // When
        boolean result = artistService.unbindArtist(dataSource, externalId);

        // Then
        assertFalse(result);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository, never()).delete(any());
    }
    
    @Test
    void searchArtistsByName_shouldReturnMatchingArtists() {
        // Given
        String search = "radio";
        Artist artist1 = Artist.builder().id(1L).name("Radiohead").build();
        Artist artist2 = Artist.builder().id(2L).name("Radio Moscow").build();
        List<Artist> artists = List.of(artist1, artist2);
        
        List<LookupResultDTO> expectedResults = List.of(
            new LookupResultDTO(1L, "Radiohead"),
            new LookupResultDTO(2L, "Radio Moscow")
        );
        
        when(artistRepository.findByNameContainingIgnoreCase(eq(search), anyInt()))
            .thenReturn(artists);
            
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(expectedResults.get(0).getId(), result.get(0).getId());
        assertEquals(expectedResults.get(0).getName(), result.get(0).getName());
        assertEquals(expectedResults.get(1).getId(), result.get(1).getId());
        assertEquals(expectedResults.get(1).getName(), result.get(1).getName());
        verify(artistRepository).findByNameContainingIgnoreCase(search, 20); // Default limit
    }
    
    @Test
    void searchArtistsByName_withLimit_shouldLimitResults() {
        // Given
        String search = "band";
        int limit = 3;
        
        // Create 5 artists
        List<Artist> artists = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> Artist.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(artistRepository.findByNameContainingIgnoreCase(eq(search), eq(limit)))
            .thenReturn(artists.subList(0, limit));
            
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search, limit);
        
        // Then
        assertEquals(limit, result.size());
        for (int i = 0; i < limit; i++) {
            assertEquals((long) (i + 1), result.get(i).getId());
            assertEquals("Band " + (i + 1), result.get(i).getName());
        }
        verify(artistRepository).findByNameContainingIgnoreCase(search, limit);
    }
    
    @Test
    void searchArtistsByName_withDefaultLimit_shouldLimitToDefaultResults() {
        // Given
        String search = "band";
        int defaultLimit = 20;
        
        // Create 30 artists (more than default limit of 20)
        List<Artist> artists = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Artist.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(artistRepository.findByNameContainingIgnoreCase(eq(search), eq(defaultLimit)))
            .thenReturn(artists.subList(0, defaultLimit));
            
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search);
        
        // Then
        assertEquals(defaultLimit, result.size());
        for (int i = 0; i < defaultLimit; i++) {
            assertEquals((long) (i + 1), result.get(i).getId());
            assertEquals("Band " + (i + 1), result.get(i).getName());
        }
        verify(artistRepository).findByNameContainingIgnoreCase(search, defaultLimit);
    }
    
    @Test
    void searchArtistsByName_withNullLimit_shouldUseDefaultLimit() {
        // Given
        String search = "band";
        Integer limit = null;
        int defaultLimit = 20;
        
        // Create 30 artists (more than default limit of 20)
        List<Artist> artists = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Artist.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(artistRepository.findByNameContainingIgnoreCase(eq(search), eq(defaultLimit)))
            .thenReturn(artists.subList(0, defaultLimit));
            
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search, limit);
        
        // Then
        assertEquals(defaultLimit, result.size());
        verify(artistRepository).findByNameContainingIgnoreCase(search, defaultLimit);
    }
    
    @Test
    void searchArtistsByName_shouldReturnSortedResults() {
        // Given
        String search = "band";
        
        // Create artists in non-alphabetical order
        Artist artist1 = Artist.builder().id(1L).name("Band C").build();
        Artist artist2 = Artist.builder().id(2L).name("Band A").build();
        Artist artist3 = Artist.builder().id(3L).name("Band B").build();
        
        // The repository should return them in alphabetical order due to ORDER BY clause
        List<Artist> sortedArtists = List.of(
            artist2, // Band A
            artist3, // Band B
            artist1  // Band C
        );
        
        when(artistRepository.findByNameContainingIgnoreCase(eq(search), anyInt()))
            .thenReturn(sortedArtists);
            
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search);
        
        // Then
        assertEquals(3, result.size());
        assertEquals("Band A", result.get(0).getName());
        assertEquals("Band B", result.get(1).getName());
        assertEquals("Band C", result.get(2).getName());
        verify(artistRepository).findByNameContainingIgnoreCase(search, 20);
    }
    
    @Test
    void searchArtistsByName_withEmptySearchTerm_shouldReturnEmptyList() {
        // Given
        String search = "";
        
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search);
        
        // Then
        assertTrue(result.isEmpty());
        verify(artistRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }
    
    @Test
    void searchArtistsByName_withNullSearchTerm_shouldReturnEmptyList() {
        // Given
        String search = null;
        
        // When
        List<LookupResultDTO> result = artistService.searchArtistsByName(search);
        
        // Then
        assertTrue(result.isEmpty());
        verify(artistRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }
}
