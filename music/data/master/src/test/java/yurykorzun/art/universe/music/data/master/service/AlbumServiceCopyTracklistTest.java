package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumTrackRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceCopyTracklistTest {

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
            albumRepository,
            albumBindingRepository,
            albumCategoryRepository,
            categoryRepository,
            albumTrackRepository,
            relationTypeApplicabilityRepository,
            trackRepository,
            trackBindingRepository,
            artistService,
            relationService,
            entityManager
        );
    }

    @Test
    void copyTracklist_copiesAllTracks() {
        // Given
        Long targetAlbumId = 1L;
        Long sourceAlbumId = 2L;
        Long relTypeId = 100L;

        AlbumTrack t1 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(10L).trackOrder(1).relationTypeId(relTypeId).build();
        AlbumTrack t2 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(20L).trackOrder(2).relationTypeId(relTypeId).build();
        AlbumTrack t3 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(30L).trackOrder(3).relationTypeId(relTypeId).build();

        when(albumRepository.existsById(targetAlbumId)).thenReturn(true);
        when(albumRepository.existsById(sourceAlbumId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumId(sourceAlbumId)).thenReturn(List.of(t1, t2, t3));
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(eq(targetAlbumId), anyLong(), any()))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        int result = albumService.copyTracklist(targetAlbumId, sourceAlbumId);

        // Then
        assertEquals(3, result);
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository, times(3)).save(captor.capture());

        List<AlbumTrack> saved = captor.getAllValues();
        assertEquals(targetAlbumId, saved.get(0).getAlbumId());
        assertEquals(10L, saved.get(0).getTrackId());
        assertEquals(1, saved.get(0).getTrackOrder());
        assertEquals(relTypeId, saved.get(0).getRelationTypeId());

        assertEquals(targetAlbumId, saved.get(1).getAlbumId());
        assertEquals(20L, saved.get(1).getTrackId());
        assertEquals(2, saved.get(1).getTrackOrder());

        assertEquals(targetAlbumId, saved.get(2).getAlbumId());
        assertEquals(30L, saved.get(2).getTrackId());
        assertEquals(3, saved.get(2).getTrackOrder());
    }

    @Test
    void copyTracklist_skipsExistingTracks() {
        // Given
        Long targetAlbumId = 1L;
        Long sourceAlbumId = 2L;
        Long relTypeId = 100L;

        AlbumTrack sourceTrack = AlbumTrack.builder().albumId(sourceAlbumId).trackId(10L).trackOrder(1).relationTypeId(relTypeId).build();
        AlbumTrack existingInTarget = AlbumTrack.builder().albumId(targetAlbumId).trackId(10L).trackOrder(1).relationTypeId(relTypeId).build();

        when(albumRepository.existsById(targetAlbumId)).thenReturn(true);
        when(albumRepository.existsById(sourceAlbumId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumId(sourceAlbumId)).thenReturn(List.of(sourceTrack));
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(targetAlbumId, 10L, relTypeId))
            .thenReturn(Optional.of(existingInTarget));

        // When
        int result = albumService.copyTracklist(targetAlbumId, sourceAlbumId);

        // Then
        assertEquals(0, result);
        verify(albumTrackRepository, never()).save(any());
    }

    @Test
    void copyTracklist_partialSkip() {
        // Given
        Long targetAlbumId = 1L;
        Long sourceAlbumId = 2L;
        Long relTypeId = 100L;

        AlbumTrack t1 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(10L).trackOrder(1).relationTypeId(relTypeId).build();
        AlbumTrack t2 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(20L).trackOrder(2).relationTypeId(relTypeId).build();
        AlbumTrack t3 = AlbumTrack.builder().albumId(sourceAlbumId).trackId(30L).trackOrder(3).relationTypeId(relTypeId).build();
        AlbumTrack existingInTarget = AlbumTrack.builder().albumId(targetAlbumId).trackId(20L).trackOrder(2).relationTypeId(relTypeId).build();

        when(albumRepository.existsById(targetAlbumId)).thenReturn(true);
        when(albumRepository.existsById(sourceAlbumId)).thenReturn(true);
        when(albumTrackRepository.findByAlbumId(sourceAlbumId)).thenReturn(List.of(t1, t2, t3));
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(targetAlbumId, 10L, relTypeId))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(targetAlbumId, 20L, relTypeId))
            .thenReturn(Optional.of(existingInTarget));
        when(albumTrackRepository.findByAlbumIdAndTrackIdAndRelationTypeId(targetAlbumId, 30L, relTypeId))
            .thenReturn(Optional.empty());
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        int result = albumService.copyTracklist(targetAlbumId, sourceAlbumId);

        // Then
        assertEquals(2, result);
        verify(albumTrackRepository, times(2)).save(any());
    }

    @Test
    void copyTracklist_throwsWhenTargetNotFound() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(false);

        // When / Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.copyTracklist(1L, 2L));

        verify(albumTrackRepository, never()).findByAlbumId(any());
    }

    @Test
    void copyTracklist_throwsWhenSourceNotFound() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(albumRepository.existsById(2L)).thenReturn(false);

        // When / Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.copyTracklist(1L, 2L));

        verify(albumTrackRepository, never()).findByAlbumId(any());
    }
}
