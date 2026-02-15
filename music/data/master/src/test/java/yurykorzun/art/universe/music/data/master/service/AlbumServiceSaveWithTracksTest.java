package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.AlbumTrackItemDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithTracksSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.entity.Album;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumTrackRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceSaveWithTracksTest {

    @Mock private AlbumRepository albumRepository;
    @Mock private AlbumBindingRepository albumBindingRepository;
    @Mock private AlbumCategoryRepository albumCategoryRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private AlbumTrackRepository albumTrackRepository;
    @Mock private RelationTypeApplicabilityRepository relationTypeApplicabilityRepository;
    @Mock private TrackRepository trackRepository;
    @Mock private EntityManager entityManager;

    private AlbumServiceImpl albumService;

    @BeforeEach
    void setUp() {
        albumService = new AlbumServiceImpl(
            albumRepository,
            albumBindingRepository,
            albumCategoryRepository,
            categoryRepository,
            albumTrackRepository,
            relationTypeApplicabilityRepository,
            trackRepository,
            entityManager
        );
    }

    @Test
    void saveAlbumWithTracks_createsNewAlbumAndTracks() {
        // Given
        Long defaultRelTypeId = 100L;
        Long albumId = 1L;
        Long trackId1 = 10L;
        Long trackId2 = 20L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Test Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId1).trackOrder(1).build(),
                AlbumTrackItemDTO.builder().trackId(trackId2).trackOrder(2).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Test Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.of(buildApplicability(defaultRelTypeId)));
        when(trackRepository.existsById(trackId1)).thenReturn(true);
        when(trackRepository.existsById(trackId2)).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId1, defaultRelTypeId))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId2, defaultRelTypeId))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        AlbumDto result = albumService.saveAlbumWithTracks(request);

        // Then
        assertEquals(albumId, result.getId());
        assertEquals("Test Album", result.getName());

        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository, times(2)).save(captor.capture());

        List<AlbumTrack> savedTracks = captor.getAllValues();
        assertEquals(albumId, savedTracks.get(0).getAlbumId());
        assertEquals(trackId1, savedTracks.get(0).getTrackId());
        assertEquals(1, savedTracks.get(0).getTrackOrder());
        assertEquals(defaultRelTypeId, savedTracks.get(0).getRelationTypeId());

        assertEquals(albumId, savedTracks.get(1).getAlbumId());
        assertEquals(trackId2, savedTracks.get(1).getTrackId());
        assertEquals(2, savedTracks.get(1).getTrackOrder());
        assertEquals(defaultRelTypeId, savedTracks.get(1).getRelationTypeId());
    }

    @Test
    void saveAlbumWithTracks_usesProvidedRelationType() {
        // Given
        Long defaultRelTypeId = 100L;
        Long customRelTypeId = 200L;
        Long albumId = 1L;
        Long trackId = 10L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Test Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId).trackOrder(1).relationTypeId(customRelTypeId).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Test Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.of(buildApplicability(defaultRelTypeId)));
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId, customRelTypeId))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        albumService.saveAlbumWithTracks(request);

        // Then
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository).save(captor.capture());

        assertEquals(customRelTypeId, captor.getValue().getRelationTypeId());
    }

    @Test
    void saveAlbumWithTracks_updatesExistingTrackOrder() {
        // Given
        Long defaultRelTypeId = 100L;
        Long albumId = 1L;
        Long trackId = 10L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Test Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId).trackOrder(5).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Test Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        AlbumTrack existingAlbumTrack = AlbumTrack.builder()
            .albumId(albumId).trackId(trackId).trackOrder(1).relationTypeId(defaultRelTypeId).build();

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.of(buildApplicability(defaultRelTypeId)));
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId, defaultRelTypeId))
            .thenReturn(Optional.of(existingAlbumTrack));
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        albumService.saveAlbumWithTracks(request);

        // Then
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository).save(captor.capture());

        AlbumTrack saved = captor.getValue();
        assertSame(existingAlbumTrack, saved);
        assertEquals(5, saved.getTrackOrder());
    }

    @Test
    void saveAlbumWithTracks_throwsWhenTrackNotFound() {
        // Given
        Long albumId = 1L;
        Long missingTrackId = 999L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Test Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(missingTrackId).trackOrder(1).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Test Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(trackRepository.existsById(missingTrackId)).thenReturn(false);

        // When / Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.saveAlbumWithTracks(request));

        verify(albumTrackRepository, never()).save(any());
    }

    @Test
    void saveAlbumWithTracks_worksWithNoDefaultRelationType() {
        // Given — no default relation type configured
        Long albumId = 1L;
        Long trackId = 10L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Test Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId).trackOrder(1).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Test Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(albumId, trackId, null))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        albumService.saveAlbumWithTracks(request);

        // Then — relation type is null but track is still created
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository).save(captor.capture());

        assertNull(captor.getValue().getRelationTypeId());
        assertEquals(1, captor.getValue().getTrackOrder());
    }

    @Test
    void saveAlbumWithTracks_updatesExistingAlbum() {
        // Given
        Long albumId = 1L;
        Long trackId = 10L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .id(albumId)
            .name("Updated Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId).trackOrder(1).build()
            ))
            .build();

        Album existingAlbum = Album.builder().name("Old Album").primaryArtistId(5L).build();
        setAlbumId(existingAlbum, albumId);

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existingAlbum));
        when(albumRepository.save(any(Album.class))).thenReturn(existingAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.empty());
        when(trackRepository.existsById(trackId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(eq(albumId), eq(trackId), any()))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        AlbumDto result = albumService.saveAlbumWithTracks(request);

        // Then
        assertEquals(albumId, result.getId());
        assertEquals("Updated Album", existingAlbum.getName());
        verify(albumRepository).findById(albumId);
    }

    @Test
    void saveAlbumWithTracks_mixedProvidedAndDefaultRelationTypes() {
        // Given
        Long defaultRelTypeId = 100L;
        Long customRelTypeId = 200L;
        Long albumId = 1L;
        Long trackId1 = 10L;
        Long trackId2 = 20L;

        AlbumWithTracksSaveRequestDTO request = AlbumWithTracksSaveRequestDTO.builder()
            .name("Mixed Album")
            .primaryArtistId(5L)
            .tracks(List.of(
                AlbumTrackItemDTO.builder().trackId(trackId1).trackOrder(1).build(),
                AlbumTrackItemDTO.builder().trackId(trackId2).trackOrder(2).relationTypeId(customRelTypeId).build()
            ))
            .build();

        Album savedAlbum = Album.builder().name("Mixed Album").primaryArtistId(5L).build();
        setAlbumId(savedAlbum, albumId);

        when(albumRepository.save(any(Album.class))).thenReturn(savedAlbum);
        when(relationTypeApplicabilityRepository
            .findBySourceEntityTypeAndTargetEntityTypeAndIsDefaultTrue(MasterEntityType.ALBUM, MasterEntityType.TRACK))
            .thenReturn(Optional.of(buildApplicability(defaultRelTypeId)));
        when(trackRepository.existsById(anyLong())).thenReturn(true);
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(anyLong(), anyLong(), any()))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        albumService.saveAlbumWithTracks(request);

        // Then
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository, times(2)).save(captor.capture());

        List<AlbumTrack> saved = captor.getAllValues();
        // First track: no relation type provided → uses default
        assertEquals(defaultRelTypeId, saved.get(0).getRelationTypeId());
        assertEquals(1, saved.get(0).getTrackOrder());
        // Second track: custom relation type provided
        assertEquals(customRelTypeId, saved.get(1).getRelationTypeId());
        assertEquals(2, saved.get(1).getTrackOrder());
    }

    // --- Helpers ---

    private RelationTypeApplicability buildApplicability(Long relationTypeId) {
        return RelationTypeApplicability.builder()
            .relationTypeId(relationTypeId)
            .sourceEntityType(MasterEntityType.ALBUM)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(true)
            .build();
    }

    /**
     * Uses reflection to set the ID on an Album entity (ID setter is private).
     */
    private void setAlbumId(Album album, Long id) {
        try {
            var field = Album.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(album, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set album ID", e);
        }
    }
}
