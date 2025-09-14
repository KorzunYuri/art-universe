package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.ArtistSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
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
        assertThrows(CustomEntityNotFoundException.class, () ->
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
    void findArtists_shouldReturnPageOfArtists() {
        // Given
        String search = "radio";
        Pageable pageable = PageRequest.of(0, 10);
        
        Artist artist1 = Artist.builder().id(1L).name("Radiohead").build();
        Artist artist2 = Artist.builder().id(2L).name("Radio Moscow").build();
        List<Artist> artists = List.of(artist1, artist2);
        Page<Artist> artistPage = new PageImpl<>(artists, pageable, artists.size());
        
        when(artistRepository.findArtists(search, pageable)).thenReturn(artistPage);

        // When
        Page<ArtistDto> result = artistService.findArtists(search, pageable);

        // Then
        assertEquals(2, result.getContent().size());
        assertEquals("Radiohead", result.getContent().get(0).getName());
        assertEquals("Radio Moscow", result.getContent().get(1).getName());
        verify(artistRepository).findArtists(search, pageable);
    }

    @Test
    void findArtists_withNullSearch_shouldReturnAllArtists() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        
        Artist artist = Artist.builder().id(1L).name("Test Artist").build();
        Page<Artist> artistPage = new PageImpl<>(List.of(artist), pageable, 1);
        
        when(artistRepository.findArtists(null, pageable)).thenReturn(artistPage);

        // When
        Page<ArtistDto> result = artistService.findArtists(null, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Test Artist", result.getContent().get(0).getName());
        verify(artistRepository).findArtists(null, pageable);
    }

    @Test
    void getArtist_shouldReturnArtist() {
        // Given
        Long id = 1L;
        Artist artist = Artist.builder().id(id).name("Test Artist").build();
        
        when(artistRepository.findById(id)).thenReturn(Optional.of(artist));

        // When
        ArtistDto result = artistService.getArtist(id);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Test Artist", result.getName());
        verify(artistRepository).findById(id);
    }

    @Test
    void getArtist_whenNotFound_shouldThrowException() {
        // Given
        Long id = 1L;
        
        when(artistRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            artistService.getArtist(id));
        
        assertEquals("Artist not found with id: " + id, exception.getMessage());
        verify(artistRepository).findById(id);
    }

    @Test
    void saveArtist_shouldCreateNewArtist() {
        // Given
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .name("New Artist")
            .build();
        
        Artist savedArtist = Artist.builder()
            .id(1L)
            .name("New Artist")
            .build();
        
        when(artistRepository.save(any(Artist.class))).thenReturn(savedArtist);

        // When
        ArtistDto result = artistService.saveArtist(request);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("New Artist", result.getName());
        verify(artistRepository).save(any(Artist.class));
    }

    @Test
    void saveArtist_shouldUpdateExistingArtist() {
        // Given
        Long artistId = 1L;
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .id(artistId)
            .name("Updated Artist")
            .build();
        
        Artist existingArtist = Artist.builder()
            .id(artistId)
            .name("Original Artist")
            .build();
        
        Artist savedArtist = Artist.builder()
            .id(artistId)
            .name("Updated Artist")
            .build();
        
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existingArtist));
        when(artistRepository.save(existingArtist)).thenReturn(savedArtist);

        // When
        ArtistDto result = artistService.saveArtist(request);

        // Then
        assertEquals(artistId, result.getId());
        assertEquals("Updated Artist", result.getName());
        verify(artistRepository).findById(artistId);
        verify(artistRepository).save(existingArtist);
    }

    @Test
    void saveArtist_whenArtistNotFound_shouldThrowException() {
        // Given
        Long artistId = 1L;
        ArtistSaveRequestDTO request = ArtistSaveRequestDTO.builder()
            .id(artistId)
            .name("Updated Artist")
            .build();
        
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            artistService.saveArtist(request));
        
        assertEquals("Artist not found with id: " + artistId, exception.getMessage());
        verify(artistRepository).findById(artistId);
        verify(artistRepository, never()).save(any());
    }

    @Test
    void deleteArtist_whenFound_shouldReturnTrue() {
        // Given
        Long id = 1L;
        
        when(artistRepository.existsById(id)).thenReturn(true);

        // When
        boolean result = artistService.deleteArtist(id);

        // Then
        assertTrue(result);
        verify(artistRepository).existsById(id);
        verify(artistRepository).deleteById(id);
    }

    @Test
    void deleteArtist_whenNotFound_shouldReturnFalse() {
        // Given
        Long id = 1L;
        
        when(artistRepository.existsById(id)).thenReturn(false);

        // When
        boolean result = artistService.deleteArtist(id);

        // Then
        assertFalse(result);
        verify(artistRepository).existsById(id);
        verify(artistRepository, never()).deleteById(any());
    }
}
