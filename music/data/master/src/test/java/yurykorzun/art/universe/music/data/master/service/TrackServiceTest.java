package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.binding.ArtistRelatedEntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
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

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private TrackServiceImpl trackService;

    // ---- findBoundTracks ----

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

    // ---- bindToExisting ----

    @Test
    void bindToExisting_whenMasterPrimaryArtistIdIsNull_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;

        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(200L)
            .masterPrimaryArtistId(null)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> trackService.bindToExisting(dataSource, externalId, request));

        assertEquals("masterPrimaryArtistId is required", exception.getMessage());

        verifyNoInteractions(trackRepository, trackBindingRepository, relationService);
    }

    @Test
    void bindToExisting_whenArtistNotFound_shouldPropagateException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;

        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(200L)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        when(artistService.getArtist(masterPrimaryArtistId))
            .thenThrow(new RuntimeException("Artist not found"));

        // When & Then
        assertThrows(RuntimeException.class,
            () -> trackService.bindToExisting(dataSource, externalId, request));

        verify(artistService).getArtist(masterPrimaryArtistId);
        verifyNoInteractions(trackRepository, trackBindingRepository, relationService);
    }

    @Test
    void bindToExisting_withNewBinding_shouldCreateBindingAndRelation() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;
        Long masterId = 200L;

        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        Track existingTrack = Track.builder()
            .id(masterId)
            .name("Test Track")
            .primaryArtistId(masterPrimaryArtistId)
            .build();

        TrackBinding trackBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(masterId)
            .build();

        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Test Track"
        );

        when(trackRepository.findById(masterId)).thenReturn(Optional.of(existingTrack));
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(trackBinding);
        when(relationService.createInternalRelation(
            eq(MasterEntityType.ARTIST), eq(masterPrimaryArtistId),
            eq(MasterEntityType.TRACK), eq(masterId), isNull()
        )).thenReturn(500L);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);

        verify(artistService).getArtist(masterPrimaryArtistId);
        verify(trackRepository).findById(masterId);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(relationService).createInternalRelation(
            eq(MasterEntityType.ARTIST), eq(masterPrimaryArtistId),
            eq(MasterEntityType.TRACK), eq(masterId), isNull()
        );
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    @Test
    void bindToExisting_whenBindingAlreadyExists_shouldUpdateAndCreateRelation() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;
        Long masterId = 200L;

        ArtistRelatedEntityBindToExistingRequestDTO request = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        Track existingTrack = Track.builder()
            .id(masterId)
            .name("Test Track")
            .primaryArtistId(masterPrimaryArtistId)
            .build();

        TrackBinding existingBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(999L) // different master ID — should be updated
            .build();

        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Test Track"
        );

        when(trackRepository.findById(masterId)).thenReturn(Optional.of(existingTrack));
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(existingBinding);
        when(relationService.createInternalRelation(
            eq(MasterEntityType.ARTIST), eq(masterPrimaryArtistId),
            eq(MasterEntityType.TRACK), eq(masterId), isNull()
        )).thenReturn(500L);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        verify(trackBindingRepository).save(any(TrackBinding.class)); // binding was updated
    }

    // ---- createAndBind ----

    @Test
    void createAndBind_whenMasterPrimaryArtistIdIsNull_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;

        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("New Track")
            .masterPrimaryArtistId(null)
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> trackService.createAndBind(dataSource, externalId, request));

        assertEquals("masterPrimaryArtistId is required", exception.getMessage());

        verifyNoInteractions(trackRepository, trackBindingRepository, relationService);
    }

    @Test
    void createAndBind_whenTrackAlreadyExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;
        String trackName = "Existing Track";

        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        Track existingTrack = Track.builder()
            .id(200L)
            .name(trackName)
            .primaryArtistId(masterPrimaryArtistId)
            .build();

        when(trackRepository.findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId))
            .thenReturn(Optional.of(existingTrack));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> trackService.createAndBind(dataSource, externalId, request));

        assertEquals(
            String.format("Track with name '%s' for artist ID %d already exists", trackName, masterPrimaryArtistId),
            exception.getMessage()
        );

        verify(artistService).getArtist(masterPrimaryArtistId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId);
        verifyNoMoreInteractions(trackRepository);
        verifyNoInteractions(trackBindingRepository, relationService);
    }

    @Test
    void createAndBind_whenTrackBindingAlreadyExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;
        String trackName = "New Track";

        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        TrackBinding existingBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(999L)
            .build();

        when(trackRepository.findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> trackService.createAndBind(dataSource, externalId, request));

        assertEquals(
            String.format("Track binding for external ID %d from %s already exists", externalId, dataSource),
            exception.getMessage()
        );

        verify(artistService).getArtist(masterPrimaryArtistId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verifyNoMoreInteractions(trackRepository, trackBindingRepository);
        verifyNoInteractions(relationService);
    }

    @Test
    void createAndBind_whenTrackDoesNotExist_shouldCreateTrackAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterPrimaryArtistId = 100L;
        String trackName = "New Track";

        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(trackName)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        Track newTrack = Track.builder()
            .id(200L)
            .name(trackName)
            .primaryArtistId(masterPrimaryArtistId)
            .build();

        TrackBinding trackBinding = TrackBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(newTrack.getId())
            .build();

        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 200L, trackName
        );

        when(trackRepository.findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(trackRepository.save(any(Track.class))).thenReturn(newTrack);
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(trackBinding);
        when(relationService.createInternalRelation(
            eq(MasterEntityType.ARTIST), eq(masterPrimaryArtistId),
            eq(MasterEntityType.TRACK), eq(newTrack.getId()), isNull()
        )).thenReturn(500L);
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, List.of(externalId)))
            .thenReturn(List.of(expectedResult));

        // When
        BoundEntityProjection result = trackService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);

        verify(artistService).getArtist(masterPrimaryArtistId);
        verify(trackRepository).findByNameAndPrimaryArtistId(trackName, masterPrimaryArtistId);
        verify(trackBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(trackRepository).save(any(Track.class));
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(relationService).createInternalRelation(
            eq(MasterEntityType.ARTIST), eq(masterPrimaryArtistId),
            eq(MasterEntityType.TRACK), eq(newTrack.getId()), isNull()
        );
        verify(trackBindingRepository).findBoundTracksForDataSource(dataSource, List.of(externalId));
    }

    // ---- unbindTrack ----

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
