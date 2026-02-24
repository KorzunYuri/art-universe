package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.TrackReorderItemDTO;
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
public class AlbumServiceReorderTracksTest {

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
    void reorderTracks_updatesOrders() {
        // Given
        Long albumId = 1L;

        AlbumTrack at1 = AlbumTrack.builder().albumId(albumId).trackId(10L).trackOrder(1).build();
        AlbumTrack at2 = AlbumTrack.builder().albumId(albumId).trackId(20L).trackOrder(2).build();
        AlbumTrack at3 = AlbumTrack.builder().albumId(albumId).trackId(30L).trackOrder(3).build();
        setAlbumTrackId(at1, 101L);
        setAlbumTrackId(at2, 102L);
        setAlbumTrackId(at3, 103L);

        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(101L).newOrder(3).build(),
            TrackReorderItemDTO.builder().albumTrackId(102L).newOrder(1).build(),
            TrackReorderItemDTO.builder().albumTrackId(103L).newOrder(2).build()
        );

        when(albumRepository.existsById(albumId)).thenReturn(true);
        when(albumTrackRepository.findById(101L)).thenReturn(Optional.of(at1));
        when(albumTrackRepository.findById(102L)).thenReturn(Optional.of(at2));
        when(albumTrackRepository.findById(103L)).thenReturn(Optional.of(at3));
        when(albumTrackRepository.save(any(AlbumTrack.class))).thenAnswer(i -> i.getArgument(0));

        // When
        albumService.reorderTracks(albumId, items);

        // Then
        ArgumentCaptor<AlbumTrack> captor = ArgumentCaptor.forClass(AlbumTrack.class);
        verify(albumTrackRepository, times(3)).save(captor.capture());

        List<AlbumTrack> saved = captor.getAllValues();
        assertEquals(3, saved.get(0).getTrackOrder()); // at1 moved to 3
        assertEquals(1, saved.get(1).getTrackOrder()); // at2 moved to 1
        assertEquals(2, saved.get(2).getTrackOrder()); // at3 moved to 2
    }

    @Test
    void reorderTracks_throwsOnDuplicateNewOrder() {
        // Given
        Long albumId = 1L;
        when(albumRepository.existsById(albumId)).thenReturn(true);

        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(101L).newOrder(1).build(),
            TrackReorderItemDTO.builder().albumTrackId(102L).newOrder(1).build()  // duplicate order
        );

        // When / Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.reorderTracks(albumId, items));

        verify(albumTrackRepository, never()).save(any());
    }

    @Test
    void reorderTracks_throwsWhenAlbumTrackNotFound() {
        // Given
        Long albumId = 1L;
        when(albumRepository.existsById(albumId)).thenReturn(true);
        when(albumTrackRepository.findById(999L)).thenReturn(Optional.empty());

        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(999L).newOrder(1).build()
        );

        // When / Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.reorderTracks(albumId, items));
    }

    @Test
    void reorderTracks_throwsWhenAlbumTrackBelongsToDifferentAlbum() {
        // Given
        Long albumId = 1L;
        Long otherAlbumId = 99L;

        AlbumTrack at = AlbumTrack.builder().albumId(otherAlbumId).trackId(10L).trackOrder(1).build();
        setAlbumTrackId(at, 101L);

        when(albumRepository.existsById(albumId)).thenReturn(true);
        when(albumTrackRepository.findById(101L)).thenReturn(Optional.of(at));

        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(101L).newOrder(1).build()
        );

        // When / Then
        assertThrows(IllegalArgumentException.class,
            () -> albumService.reorderTracks(albumId, items));

        verify(albumTrackRepository, never()).save(any());
    }

    @Test
    void reorderTracks_throwsWhenAlbumNotFound() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(false);

        List<TrackReorderItemDTO> items = List.of(
            TrackReorderItemDTO.builder().albumTrackId(101L).newOrder(1).build()
        );

        // When / Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> albumService.reorderTracks(1L, items));

        verify(albumTrackRepository, never()).findById(any());
    }

    // --- Helpers ---

    private void setAlbumTrackId(AlbumTrack albumTrack, Long id) {
        try {
            var field = AlbumTrack.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(albumTrack, id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set AlbumTrack ID", e);
        }
    }
}
