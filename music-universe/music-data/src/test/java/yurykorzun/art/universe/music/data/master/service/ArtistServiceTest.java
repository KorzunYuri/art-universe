package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistBindingRepository artistBindingRepository;
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private Query query;

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
    void unbindArtist_whenBindingExists_shouldDeleteBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        ArtistBinding existingBinding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(101L)
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
    
    @Test
    void bindToExisting_whenArtistExists_shouldCreateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistId = 101L;
        
        Artist existingArtist = Artist.builder()
            .id(artistId)
            .name("Radiohead")
            .build();
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(artistId)
            .build();
        
        ArtistBinding binding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(artistId)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, artistId, "Radiohead"
        );
        
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existingArtist));
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(artistBindingRepository.save(any(ArtistBinding.class))).thenReturn(binding);
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = artistService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistRepository).findById(artistId);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(any(ArtistBinding.class));
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }

    @Test
    void bindToExisting_whenArtistDoesNotExist_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistId = 101L;
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(artistId)
            .build();
        
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> 
            artistService.bindToExisting(dataSource, externalId, request));
        
        verify(artistRepository).findById(artistId);
        verify(artistBindingRepository, never()).save(any());
    }

    @Test
    void bindToExisting_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistId = 101L;
        Long oldArtistId = 102L;
        
        Artist existingArtist = Artist.builder()
            .id(artistId)
            .name("Radiohead")
            .build();
        
        ArtistBinding existingBinding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(oldArtistId)
            .build();
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(artistId)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, artistId, "Radiohead"
        );
        
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existingArtist));
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(artistBindingRepository.save(existingBinding)).thenReturn(existingBinding);
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = artistService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        assertEquals(artistId, existingBinding.getMasterId());
        
        verify(artistRepository).findById(artistId);
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(existingBinding);
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }

    @Test
    void createAndBind_shouldCreateArtistAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "New Artist";
        
        Artist newArtist = Artist.builder()
            .id(101L)
            .name(artistName)
            .build();
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        ArtistBinding binding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(newArtist.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newArtist.getId(), artistName
        );
        
        when(artistRepository.save(any(Artist.class))).thenReturn(newArtist);
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(artistBindingRepository.save(any(ArtistBinding.class))).thenReturn(binding);
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = artistService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistRepository).save(any(Artist.class));
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(any(ArtistBinding.class));
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }

    @Test
    void createAndBind_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "New Artist";
        
        Artist newArtist = Artist.builder()
            .id(101L)
            .name(artistName)
            .build();
        
        ArtistBinding existingBinding = ArtistBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(999L) // Old reference
            .build();
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newArtist.getId(), artistName
        );
        
        when(artistRepository.save(any(Artist.class))).thenReturn(newArtist);
        when(artistBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(artistBindingRepository.save(existingBinding)).thenReturn(existingBinding);
        when(artistBindingRepository.findBoundArtistForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = artistService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        assertEquals(newArtist.getId(), existingBinding.getMasterId());
        
        verify(artistRepository).save(any(Artist.class));
        verify(artistBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(artistBindingRepository).save(existingBinding);
        verify(artistBindingRepository).findBoundArtistForDataSource(dataSource, externalId);
    }
    @Test
    void createAndBind_whenArtistWithNameExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String artistName = "Existing Artist";
        
        Artist existingArtist = Artist.builder()
            .id(999L)
            .name(artistName)
            .build();
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(artistName)
            .build();
        
        when(artistRepository.findByName(artistName)).thenReturn(Optional.of(existingArtist));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            artistService.createAndBind(dataSource, externalId, request));
        
        assertEquals("Artist with name Existing Artist already exists", exception.getMessage());
        
        verify(artistRepository).findByName(artistName);
        verify(artistRepository, never()).save(any(Artist.class));
        verify(artistBindingRepository, never()).save(any(ArtistBinding.class));
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
    void batchLookupArtists_shouldReturnResultsForMultipleSearchTerms() {
        // Given
        List<String> searchTerms = List.of("radio", "queen");
        int limit = 10;
        
        BatchLookupRequestDTO request = BatchLookupRequestDTO.builder()
            .searchTerms(searchTerms)
            .limit(limit)
            .build();
        
        // Mock the dynamic SQL query execution
        List<Object[]> queryResults = new ArrayList<>();
        // Results for "radio"
        queryResults.add(new Object[]{1L, "Radiohead", null, null, "radio"});
        queryResults.add(new Object[]{2L, "Radio Moscow", null, null, "radio"});
        // Results for "queen"
        queryResults.add(new Object[]{3L, "Queen", null, null, "queen"});
        
        // Set up EntityManager and Query mocks
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(queryResults);
        
        // When
        BatchLookupResponseDTO result = artistService.batchLookupArtists(request);
        
        // Then
        assertNotNull(result);
        assertNotNull(result.getResults());
        assertEquals(2, result.getResults().size());
        
        // Check "radio" results
        List<LookupResultDTO> radioResults = result.getResults().get("radio");
        assertNotNull(radioResults);
        assertEquals(2, radioResults.size());
        assertEquals("Radiohead", radioResults.get(0).getName());
        assertEquals("Radio Moscow", radioResults.get(1).getName());
        
        // Check "queen" results
        List<LookupResultDTO> queenResults = result.getResults().get("queen");
        assertNotNull(queenResults);
        assertEquals(1, queenResults.size());
        assertEquals("Queen", queenResults.get(0).getName());
        
        // Verify EntityManager and Query interactions
        verify(entityManager).createNativeQuery(anyString());
        // 6 parameters: 2 search terms * (1 for search_term column + 1 for WHERE clause + 1 for LIMIT)
        verify(query, times(6)).setParameter(anyInt(), any());
        verify(query).getResultList();
    }
    
    @Test
    void batchLookupArtists_withNullSearchTerms_shouldReturnEmptyResults() {
        // Given
        BatchLookupRequestDTO request = BatchLookupRequestDTO.builder()
            .searchTerms(null)
            .limit(10)
            .build();
        
        // When
        BatchLookupResponseDTO result = artistService.batchLookupArtists(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        verify(entityManager, never()).createNativeQuery(anyString());
    }
    
    @Test
    void batchLookupArtists_withEmptySearchTerms_shouldReturnEmptyResults() {
        // Given
        BatchLookupRequestDTO request = BatchLookupRequestDTO.builder()
            .searchTerms(List.of())
            .limit(10)
            .build();
        
        // When
        BatchLookupResponseDTO result = artistService.batchLookupArtists(request);
        
        // Then
        assertNotNull(result);
        assertTrue(result.getResults().isEmpty());
        verify(entityManager, never()).createNativeQuery(anyString());
    }
    
    @Test
    void batchLookupArtists_withNullLimit_shouldUseDefaultLimit() {
        // Given
        List<String> searchTerms = List.of("radio");
        Integer limit = null;
        int defaultLimit = 20;
        
        BatchLookupRequestDTO request = BatchLookupRequestDTO.builder()
            .searchTerms(searchTerms)
            .limit(limit)
            .build();
        
        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{1L, "Radiohead", null, null, "radio"});
        
        // Set up EntityManager and Query mocks
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(queryResults);
        
        // When
        artistService.batchLookupArtists(request);
        
        // Then
        verify(entityManager).createNativeQuery(anyString());
        // Verify that the third parameter (index 3) is the default limit
        verify(query).setParameter(eq(3), eq(defaultLimit));
    }
    
    @Test
    void batchLookupArtists_withBlankSearchTerms_shouldFilterThemOut() {
        // Given
        List<String> searchTerms = Arrays.asList("radio", "", "  ", null);
        int limit = 10;
        
        BatchLookupRequestDTO request = BatchLookupRequestDTO.builder()
            .searchTerms(searchTerms)
            .limit(limit)
            .build();
        
        List<Object[]> queryResults = new ArrayList<>();
        queryResults.add(new Object[]{1L, "Radiohead", null, null, "radio"});
        
        // Set up EntityManager and Query mocks
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(queryResults);
        
        // When
        BatchLookupResponseDTO result = artistService.batchLookupArtists(request);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.getResults().size());
        assertTrue(result.getResults().containsKey("radio"));
        
        // Verify that only one search term was used (3 parameters: search_term, WHERE clause, LIMIT)
        verify(query, times(3)).setParameter(anyInt(), any());
    }
}
