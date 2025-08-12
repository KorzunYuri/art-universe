package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.ArtistRepository;

import java.util.List;
import java.util.Optional;

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
    

}
