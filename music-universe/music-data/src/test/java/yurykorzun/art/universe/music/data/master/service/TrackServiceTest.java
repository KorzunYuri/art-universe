package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.TrackBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackBindingRepository trackBindingRepository;

    @Mock
    private ArtistService artistService;
    
    @Mock
    private RelationService relationService;

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
    void bindToExisting_whenArtistNotBound_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        Long trackId = 200L;
        
        TrackBindToExistingRequestDTO request = TrackBindToExistingRequestDTO.builder()
            .trackId(trackId)
            .artistExternalId(artistExternalId)
            .build();
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> trackService.bindToExisting(dataSource, externalId, request));
        
        assertEquals("Artist with external ID 100 from LASTFM must be bound before binding track", 
            exception.getMessage());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verifyNoInteractions(trackRepository, relationService);
    }

    @Test
    void bindToExisting_whenTrackExists_shouldCreateRelationAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        Long trackId = 200L;
        
        TrackBindToExistingRequestDTO request = TrackBindToExistingRequestDTO.builder()
            .trackId(trackId)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 300L, "Test Artist"
        );
        
        Track existingTrack = Track.builder()
            .id(trackId)
            .name("Test Track")
            .primaryArtistId(400L) // Different from artist binding
            .build();
        
        TrackBinding trackBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(trackId)
            .build();
        
        RelationBindingDTO relationBinding = RelationBindingDTO.builder()
            .sourceExternalId(artistExternalId)
            .targetExternalId(externalId)
            .dataSource(dataSource)
            .relationId(500L)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, trackId, "Test Track"
        );
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(existingTrack));
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(trackBinding);
        when(relationService.createInternalRelation(
            eq(EntityType.ARTIST), eq(artistBinding.getMasterId()),
            eq(EntityType.TRACK), eq(trackId)
        )).thenReturn(500L);
        when(relationService.bindExternalRelation(
            eq(dataSource), 
            eq(EntityType.ARTIST), eq(artistExternalId), 
            eq(EntityType.TRACK), eq(externalId)
        )).thenReturn(relationBinding);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findById(trackId);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(relationService).createInternalRelation(
            eq(EntityType.ARTIST), eq(artistBinding.getMasterId()),
            eq(EntityType.TRACK), eq(trackId)
        );
        verify(relationService).bindExternalRelation(
            eq(dataSource), 
            eq(EntityType.ARTIST), eq(artistExternalId), 
            eq(EntityType.TRACK), eq(externalId)
        );
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void createAndBind_whenArtistNotBound_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "New Track";
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(null);

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> trackService.createAndBind(dataSource, externalId, request));
        
        assertEquals("Artist with external ID 100 from LASTFM must be bound before binding track", 
            exception.getMessage());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verifyNoInteractions(trackRepository, relationService);
    }

    @Test
    void createAndBind_whenTrackAlreadyExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "Existing Track";
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 300L, "Test Artist"
        );
        
        Track existingTrack = Track.builder()
            .id(200L)
            .name(trackName)
            .primaryArtistId(300L)
            .build();
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId()))
            .thenReturn(Optional.of(existingTrack));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> trackService.createAndBind(dataSource, externalId, request));
        
        assertEquals("Track with name 'Existing Track' for artist ID 300 already exists", 
            exception.getMessage());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId());
        verifyNoMoreInteractions(trackRepository);
        verifyNoInteractions(relationService);
    }
    
    @Test
    void createAndBind_whenTrackBindingAlreadyExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "New Track";
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 300L, "Test Artist"
        );
        
        TrackBinding existingBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(999L)
            .build();
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId()))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, 
            () -> trackService.createAndBind(dataSource, externalId, request));
        
        assertEquals("Track binding for external ID 1 from LASTFM already exists", 
            exception.getMessage());
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId());
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verifyNoMoreInteractions(trackRepository, trackBindingRepository);
        verifyNoInteractions(relationService);
    }

    @Test
    void createAndBind_whenTrackDoesNotExist_shouldCreateTrackAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long artistExternalId = 100L;
        String trackName = "New Track";
        
        TrackCreateAndBindRequestDTO request = TrackCreateAndBindRequestDTO.builder()
            .name(trackName)
            .artistExternalId(artistExternalId)
            .build();
        
        TestBoundEntityProjectionImpl artistBinding = new TestBoundEntityProjectionImpl(
            artistExternalId, dataSource, 300L, "Test Artist"
        );
        
        Track newTrack = Track.builder()
            .id(200L)
            .name(trackName)
            .primaryArtistId(300L)
            .build();
        
        TrackBinding trackBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(newTrack.getId())
            .build();
        
        RelationBindingDTO relationBinding = RelationBindingDTO.builder()
            .sourceExternalId(artistExternalId)
            .targetExternalId(externalId)
            .dataSource(dataSource)
            .relationId(500L)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 200L, trackName
        );
        
        when(artistService.findArtist(dataSource, artistExternalId)).thenReturn(artistBinding);
        when(trackRepository.findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId()))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackRepository.save(any(Track.class))).thenReturn(newTrack);
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(trackBinding);
        when(relationService.createInternalRelation(
            eq(EntityType.ARTIST), eq(artistBinding.getMasterId()),
            eq(EntityType.TRACK), eq(newTrack.getId())
        )).thenReturn(500L);
        when(relationService.bindExternalRelation(
            eq(dataSource), 
            eq(EntityType.ARTIST), eq(artistExternalId), 
            eq(EntityType.TRACK), eq(externalId)
        )).thenReturn(relationBinding);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(artistService).findArtist(dataSource, artistExternalId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, artistBinding.getMasterId());
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackRepository).save(any(Track.class));
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(relationService).createInternalRelation(
            eq(EntityType.ARTIST), eq(artistBinding.getMasterId()),
            eq(EntityType.TRACK), eq(newTrack.getId())
        );
        verify(relationService).bindExternalRelation(
            eq(dataSource), 
            eq(EntityType.ARTIST), eq(artistExternalId), 
            eq(EntityType.TRACK), eq(externalId)
        );
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
            .masterId(101L)
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
