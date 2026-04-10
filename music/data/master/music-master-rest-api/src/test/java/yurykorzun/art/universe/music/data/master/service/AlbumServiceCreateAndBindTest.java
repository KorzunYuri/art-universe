package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.ArtistDto;
import yurykorzun.art.universe.music.data.master.dto.binding.*;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceCreateAndBindTest {

    @Mock private AlbumRepository albumRepository;
    @Mock private AlbumBindingRepository albumBindingRepository;
    @Mock private AlbumCategoryRepository albumCategoryRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AlbumTrackRepository albumTrackRepository;
    @Mock private RelationTypeApplicabilityRepository relationTypeApplicabilityRepository;
    @Mock private TrackRepository trackRepository;
    @Mock private TrackBindingRepository trackBindingRepository;
    @Mock private ArtistService artistService;
    @Mock private RelationService relationService;
    @Mock private EntityManager entityManager;

    private AlbumServiceImpl albumService;

    @BeforeEach
    void setUp() {
        albumService = new AlbumServiceImpl(
            albumRepository, albumBindingRepository, albumCategoryRepository,
            categoryRepository, albumTrackRepository, relationTypeApplicabilityRepository,
            trackRepository, trackBindingRepository, artistService, relationService, entityManager);
    }

    // ============================================================
    // createAndBind
    // ============================================================

    @Test
    void createAndBind_happyPath_shouldCreateAlbumBindingAndRelation() {
        // Given
        Long artistId = 10L;
        Long externalId = 100L;
        Long savedAlbumId = 1L;

        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Abbey Road").masterPrimaryArtistId(artistId).build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).name("Beatles").build());
        when(albumRepository.findByNameAndPrimaryArtistId("Abbey Road", artistId)).thenReturn(Optional.empty());
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalId)).thenReturn(Optional.empty());

        Album savedAlbum = Album.builder().name("Abbey Road").primaryArtistId(artistId).build();
        setField(savedAlbum, "id", savedAlbumId);
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(albumBindingRepository.save(any(AlbumBinding.class))).thenReturn(mock(AlbumBinding.class));

        BoundEntityProjection projection = mock(BoundEntityProjection.class);
        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalId)))
            .thenReturn(List.of(projection));

        // When
        BoundEntityProjection result = albumService.createAndBind(DataSource.LASTFM, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        assertSame(projection, result);
        verify(albumRepository).save(any(Album.class));
        verify(albumBindingRepository).save(any(AlbumBinding.class));
        verify(relationService).createInternalRelation(
            MasterEntityType.ARTIST, artistId, MasterEntityType.ALBUM, savedAlbumId, null, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void createAndBind_whenMasterArtistIdNull_shouldThrow() {
        // Given
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Abbey Road").masterPrimaryArtistId(null).build();

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.createAndBind(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumRepository, never()).save(any());
    }

    @Test
    void createAndBind_whenAlbumAlreadyExists_shouldThrow() {
        // Given
        Long artistId = 10L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Abbey Road").masterPrimaryArtistId(artistId).build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).name("Beatles").build());
        Album existing = Album.builder().name("Abbey Road").primaryArtistId(artistId).build();
        when(albumRepository.findByNameAndPrimaryArtistId("Abbey Road", artistId)).thenReturn(Optional.of(existing));

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.createAndBind(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumRepository, never()).save(any());
    }

    @Test
    void createAndBind_whenBindingAlreadyExists_shouldThrow() {
        // Given
        Long artistId = 10L;
        Long externalId = 100L;
        ArtistRelatedEntityCreateAndBindRequestDTO request = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Abbey Road").masterPrimaryArtistId(artistId).build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).name("Beatles").build());
        when(albumRepository.findByNameAndPrimaryArtistId("Abbey Road", artistId)).thenReturn(Optional.empty());
        AlbumBinding existing = AlbumBinding.builder()
            .dataSource(DataSource.LASTFM).externalId(externalId).masterId(999L).build();
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalId))
            .thenReturn(Optional.of(existing));

        // When & Then
        assertThrows(IllegalStateException.class,
            () -> albumService.createAndBind(DataSource.LASTFM, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumRepository, never()).save(any());
    }

    // ============================================================
    // createAndBindWithTracks — resolveMasterAlbum branches
    // ============================================================

    @Test
    void createAndBindWithTracks_withMasterAlbumId_albumExists_shouldUseExistingAlbum() {
        // Given
        Long artistId = 10L;
        Long masterAlbumId = 5L;
        Long externalAlbumId = 100L;
        Long externalTrackId = 200L;
        Long masterTrackId = 50L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .masterAlbumId(masterAlbumId)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).masterTrackId(masterTrackId).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumRepository.existsById(masterAlbumId)).thenReturn(true);
        when(trackRepository.existsById(masterTrackId)).thenReturn(true);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(mock(TrackBinding.class));
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(masterAlbumId, masterTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        BoundEntityProjection projection = mock(BoundEntityProjection.class);
        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(projection));

        // When
        BoundEntityProjection result = albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        assertSame(projection, result);
        verify(albumRepository, never()).save(any()); // album not created
        verify(albumRepository).existsById(masterAlbumId);
    }

    @Test
    void createAndBindWithTracks_withMasterAlbumId_albumNotFound_shouldThrow() {
        // Given
        Long artistId = 10L;
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .masterAlbumId(999L)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(1L).masterTrackId(1L).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumRepository.existsById(999L)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.createAndBindWithTracks(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
    }

    @Test
    void createAndBindWithTracks_withExistingExternalBinding_shouldUseExistingMasterId() {
        // Given
        Long artistId = 10L;
        Long externalAlbumId = 100L;
        Long existingMasterAlbumId = 7L;
        Long externalTrackId = 200L;
        Long masterTrackId = 50L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .albumName("Abbey Road")
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).masterTrackId(masterTrackId).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        AlbumBinding existingBinding = AlbumBinding.builder()
            .dataSource(DataSource.LASTFM).externalId(externalAlbumId).masterId(existingMasterAlbumId).build();
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalAlbumId))
            .thenReturn(Optional.of(existingBinding));
        when(trackRepository.existsById(masterTrackId)).thenReturn(true);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(mock(TrackBinding.class));
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(existingMasterAlbumId, masterTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        BoundEntityProjection projection = mock(BoundEntityProjection.class);
        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(projection));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then — album was NOT created (used existing binding)
        verify(albumRepository, never()).save(any());
    }

    @Test
    void createAndBindWithTracks_newAlbum_withUnboundTracks_shouldCreateEverything() {
        // Given
        Long artistId = 10L;
        Long externalAlbumId = 100L;
        Long externalTrackId = 200L;
        Long savedAlbumId = 1L;
        Long savedTrackId = 50L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .albumName("Abbey Road")
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).trackName("Come Together").trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalAlbumId))
            .thenReturn(Optional.empty());

        Album savedAlbum = Album.builder().name("Abbey Road").primaryArtistId(artistId).build();
        setField(savedAlbum, "id", savedAlbumId);
        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(albumBindingRepository.save(any(AlbumBinding.class))).thenReturn(mock(AlbumBinding.class));

        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.empty());
        Track savedTrack = Track.builder().name("Come Together").primaryArtistId(artistId).build();
        setField(savedTrack, "id", savedTrackId);
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(mock(TrackBinding.class));

        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(savedAlbumId, savedTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        BoundEntityProjection projection = mock(BoundEntityProjection.class);
        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(projection));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        verify(albumRepository).save(any(Album.class));
        verify(trackRepository).save(any(Track.class));
        verify(trackBindingRepository).save(any(TrackBinding.class));
        verify(relationService).createInternalRelation(
            MasterEntityType.ARTIST, artistId, MasterEntityType.ALBUM, savedAlbumId, null, Origin.MANUAL, MasterApprovalStatus.APPROVED);
        verify(relationService).createInternalRelation(
            MasterEntityType.ARTIST, artistId, MasterEntityType.TRACK, savedTrackId, null, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void createAndBindWithTracks_newAlbum_blankAlbumName_shouldThrow() {
        // Given
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(10L)
            .albumName("")
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(1L).masterTrackId(1L).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(10L)).thenReturn(ArtistDto.builder().id(10L).build());
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, 100L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.createAndBindWithTracks(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
    }

    // ============================================================
    // createAndBindWithTracks — resolveMasterTrack branches
    // ============================================================

    @Test
    void createAndBindWithTracks_boundTrack_existingTrackBinding_shouldSkipCreatingBinding() {
        // Given
        Long artistId = 10L;
        Long masterAlbumId = 5L;
        Long externalTrackId = 200L;
        Long masterTrackId = 50L;
        Long externalAlbumId = 100L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .masterAlbumId(masterAlbumId)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).masterTrackId(masterTrackId).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumRepository.existsById(masterAlbumId)).thenReturn(true);
        when(trackRepository.existsById(masterTrackId)).thenReturn(true);
        // Binding already exists
        TrackBinding existingBinding = TrackBinding.builder()
            .dataSource(DataSource.LASTFM).externalId(externalTrackId).masterId(masterTrackId).build();
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.of(existingBinding));

        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(masterAlbumId, masterTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(mock(BoundEntityProjection.class)));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then — track binding was NOT created (already existed)
        verify(trackBindingRepository, never()).save(any());
        verify(trackRepository, never()).save(any());
    }

    @Test
    void createAndBindWithTracks_boundTrack_notFound_shouldThrow() {
        // Given
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(10L)
            .masterAlbumId(5L)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(200L).masterTrackId(999L).trackOrder(1).build()))
            .build();

        when(artistService.getArtist(10L)).thenReturn(ArtistDto.builder().id(10L).build());
        when(albumRepository.existsById(5L)).thenReturn(true);
        when(trackRepository.existsById(999L)).thenReturn(false);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, 200L))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.createAndBindWithTracks(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
    }

    @Test
    void createAndBindWithTracks_unboundTrack_existingTrackBinding_shouldUseExistingMaster() {
        // Given
        Long artistId = 10L;
        Long masterAlbumId = 5L;
        Long externalTrackId = 200L;
        Long existingMasterTrackId = 77L;
        Long externalAlbumId = 100L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .masterAlbumId(masterAlbumId)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).trackName("Come Together").trackOrder(1).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumRepository.existsById(masterAlbumId)).thenReturn(true);
        // External track already bound — no masterTrackId provided
        TrackBinding existingBinding = TrackBinding.builder()
            .dataSource(DataSource.LASTFM).externalId(externalTrackId).masterId(existingMasterTrackId).build();
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.of(existingBinding));

        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(masterAlbumId, existingMasterTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(mock(BoundEntityProjection.class)));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then — track not created (used existing binding)
        verify(trackRepository, never()).save(any());
    }

    @Test
    void createAndBindWithTracks_unboundTrack_blankTrackName_shouldThrow() {
        // Given
        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(10L)
            .masterAlbumId(5L)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(200L).trackName("").trackOrder(1).build()))
            .build();

        when(artistService.getArtist(10L)).thenReturn(ArtistDto.builder().id(10L).build());
        when(albumRepository.existsById(5L)).thenReturn(true);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, 200L))
            .thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.createAndBindWithTracks(DataSource.LASTFM, 100L, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
    }

    @Test
    void createAndBindWithTracks_unboundTrack_withPerTrackArtistOverride_shouldUseTrackArtist() {
        // Given
        Long albumArtistId = 10L;
        Long trackArtistId = 20L;
        Long masterAlbumId = 5L;
        Long externalTrackId = 200L;
        Long savedTrackId = 50L;
        Long externalAlbumId = 100L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(albumArtistId)
            .masterAlbumId(masterAlbumId)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId)
                .trackName("Solo Track")
                .masterPrimaryArtistId(trackArtistId)   // per-track override
                .trackOrder(1).build()))
            .build();

        when(artistService.getArtist(albumArtistId)).thenReturn(ArtistDto.builder().id(albumArtistId).build());
        when(albumRepository.existsById(masterAlbumId)).thenReturn(true);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.empty());

        Track savedTrack = Track.builder().name("Solo Track").primaryArtistId(trackArtistId).build();
        setField(savedTrack, "id", savedTrackId);
        when(trackRepository.save(any(Track.class))).thenReturn(savedTrack);
        when(trackBindingRepository.save(any(TrackBinding.class))).thenReturn(mock(TrackBinding.class));

        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(masterAlbumId, savedTrackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenReturn(mock(AlbumTrack.class));

        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(mock(BoundEntityProjection.class)));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then — ARTIST→TRACK relation uses the per-track artist, not the album artist
        verify(relationService).createInternalRelation(
            MasterEntityType.ARTIST, trackArtistId, MasterEntityType.TRACK, savedTrackId, null, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void createAndBindWithTracks_updatesExistingAlbumTrack_shouldUpdateOrder() {
        // Given
        Long artistId = 10L;
        Long masterAlbumId = 5L;
        Long externalTrackId = 200L;
        Long masterTrackId = 50L;
        Long externalAlbumId = 100L;

        ExternalAlbumWithTracksCreateAndBindRequestDTO request = ExternalAlbumWithTracksCreateAndBindRequestDTO.builder()
            .masterPrimaryArtistId(artistId)
            .masterAlbumId(masterAlbumId)
            .tracks(List.of(ExternalAlbumTrackItemDTO.builder()
                .externalTrackId(externalTrackId).masterTrackId(masterTrackId).trackOrder(5).build()))
            .build();

        when(artistService.getArtist(artistId)).thenReturn(ArtistDto.builder().id(artistId).build());
        when(albumRepository.existsById(masterAlbumId)).thenReturn(true);
        when(trackRepository.existsById(masterTrackId)).thenReturn(true);
        when(trackBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, externalTrackId))
            .thenReturn(Optional.empty());
        when(trackBindingRepository.save(any())).thenReturn(mock(TrackBinding.class));

        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());

        AlbumTrack existing = AlbumTrack.builder()
            .albumId(masterAlbumId).trackId(masterTrackId).trackOrder(1).build();
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(masterAlbumId, masterTrackId, null))
            .thenReturn(Optional.of(existing));
        when(albumTrackRepository.save(existing)).thenReturn(existing);

        when(albumBindingRepository.findBoundAlbumsForDataSource(DataSource.LASTFM, List.of(externalAlbumId)))
            .thenReturn(List.of(mock(BoundEntityProjection.class)));

        // When
        albumService.createAndBindWithTracks(DataSource.LASTFM, externalAlbumId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then — order updated on existing track
        assertEquals(5, existing.getTrackOrder());
        verify(albumTrackRepository).save(existing);
    }

    // --- helper ---

    private void setField(Object target, String fieldName, Object value) {
        Class<?> clazz = target.getClass();
        while (clazz != null) {
            try {
                var field = clazz.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                throw new RuntimeException("Failed to set field " + fieldName, e);
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
