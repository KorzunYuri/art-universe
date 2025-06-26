package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.dto.TrackBindingRequestDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.Track;
import yurykorzun.art.universe.music.data.approved.entity.TrackBinding;
import yurykorzun.art.universe.music.data.approved.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackBindingRepository trackBindingRepository;

    @Mock
    private ArtistService artistService;

    @InjectMocks
    private TrackServiceImpl trackService;

    @Test
    void findBoundTracks_shouldReturnListOfBoundTracks() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Test Track"
        );
        List<BoundEntityProjection> expectedResult = List.of(projection);
        
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, externalIds))
            .thenReturn(expectedResult);

        // When
        List<BoundEntityProjection> result = trackService.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(expectedResult, result);
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, externalIds);
    }

    @Test
    void findTrack_shouldReturnSingleBoundTrack() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, "Test Track"
        );
        
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(projection));

        // When
        BoundEntityProjection result = trackService.findTrack(dataSource, externalId);

        // Then
        assertEquals(projection, result);
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void findTrack_whenNotFound_shouldReturnNull() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of());

        // When
        BoundEntityProjection result = trackService.findTrack(dataSource, externalId);

        // Then
        assertNull(result);
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void bindTrack_whenArtistNotBound_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        
        TrackBindingRequestDTO request = TrackBindingRequestDTO.builder()
            .name("Test Track")
            .artistExternalId(artistExternalId)
            .build();
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> trackService.bindTrack(dataSource, externalId, request));
        
        assertEquals("Artist with external ID 100 from LASTFM must be bound before binding track", 
            exception.getMessage());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verifyNoInteractions(trackRepository, trackBindingRepository);
    }

    @Test
    void bindTrack_whenTrackExists_shouldCreateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "Test Track";
        
        TrackBindingRequestDTO request = TrackBindingRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 200L, "Test Artist"
        );
        
        Track existingTrack = Track.builder()
            .id(101L)
            .name(trackName)
            .primaryArtistId(200L)
            .build();
        
        TrackBinding binding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(existingTrack.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, existingTrack.getId(), trackName
        );
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, 200L))
            .thenReturn(Optional.of(existingTrack));
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(binding);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindTrack(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, 200L);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void bindTrack_whenTrackDoesNotExist_shouldCreateTrackAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "New Track";
        
        TrackBindingRequestDTO request = TrackBindingRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 200L, "Test Artist"
        );
        
        Track newTrack = Track.builder()
            .id(101L)
            .name(trackName)
            .primaryArtistId(200L)
            .build();
        
        TrackBinding binding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(newTrack.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newTrack.getId(), trackName
        );
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, 200L))
            .thenReturn(Optional.empty());
        when(trackRepository.save(any(Track.class))).thenReturn(newTrack);
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(binding);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindTrack(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, 200L);
        verify(trackRepository).save(any(Track.class));
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void bindTrack_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "Test Track";
        
        TrackBindingRequestDTO request = TrackBindingRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 200L, "Test Artist"
        );
        
        Track existingTrack = Track.builder()
            .id(101L)
            .name(trackName)
            .primaryArtistId(200L)
            .build();
        
        TrackBinding existingBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(999L) // Different track ID
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, existingTrack.getId(), trackName
        );
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, 200L))
            .thenReturn(Optional.of(existingTrack));
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(trackBindingRepository.save(existingBinding)).thenReturn(existingBinding);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindTrack(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        assertEquals(existingTrack.getId(), existingBinding.getReferenceId());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, 200L);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).save(existingBinding);
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void unbindTrack_whenBindingExists_shouldDeleteBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        TrackBinding existingBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(101L)
            .build();
        
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        doNothing().when(trackBindingRepository).delete(existingBinding);

        // When
        boolean result = trackService.unbindTrack(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).delete(existingBinding);
    }

    @Test
    void unbindTrack_whenBindingDoesNotExist_shouldReturnFalse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());

        // When
        boolean result = trackService.unbindTrack(dataSource, externalId);

        // Then
        assertFalse(result);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository, never()).delete(any());
    }
}
