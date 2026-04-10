package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.TrackDto;
import yurykorzun.art.universe.music.data.master.dto.TrackSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TrackWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.TrackRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackServiceCrudTest {

    @Mock private TrackRepository trackRepository;
    @Mock private TrackBindingRepository bindingsRepository;
    @Mock private TrackCategoryRepository trackCategoryRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private ArtistService artistService;
    @Mock private RelationService relationService;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private TrackServiceImpl trackService;

    // --- findTracks ---

    @Test
    void findTracks_withoutCategoryId_shouldCallRepositoryWithoutCategory() {
        String search = "paranoid";
        Pageable pageable = PageRequest.of(0, 10);
        Track track = track(1L, "Paranoid Android", 10L);
        when(trackRepository.findTracks(search, pageable))
            .thenReturn(new PageImpl<>(List.of(track)));

        Page<TrackDto> result = trackService.findTracks(search, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Paranoid Android", result.getContent().get(0).getName());
        verify(trackRepository).findTracks(search, pageable);
        verify(trackRepository, never()).findTracks(anyString(), anyLong(), any());
    }

    @Test
    void findTracks_withCategoryId_shouldCallRepositoryWithCategory() {
        String search = "paranoid";
        Long categoryId = 5L;
        Pageable pageable = PageRequest.of(0, 10);
        when(trackRepository.findTracks(search, categoryId, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        trackService.findTracks(search, categoryId, pageable);

        verify(trackRepository).findTracks(search, categoryId, pageable);
        verify(trackRepository, never()).findTracks(anyString(), any(Pageable.class));
    }

    // --- findTracksWithCategories ---

    @Test
    void findTracksWithCategories_shouldDelegateToRepository() {
        String search = "test";
        Pageable pageable = PageRequest.of(0, 10);
        Track track = track(1L, "Test Track", 10L);
        when(trackRepository.findTracksWithCategories(search, null, pageable))
            .thenReturn(new PageImpl<>(List.of(track)));

        Page<TrackWithCategoriesDto> result = trackService.findTracksWithCategories(search, null, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Track", result.getContent().get(0).getName());
    }

    // --- getTrack ---

    @Test
    void getTrack_whenFound_shouldReturnDto() {
        Long id = 1L;
        when(trackRepository.findById(id)).thenReturn(Optional.of(track(id, "Test Track", 10L)));

        TrackDto result = trackService.getTrack(id);

        assertEquals(id, result.getId());
        assertEquals("Test Track", result.getName());
        assertEquals(10L, result.getPrimaryArtistId());
    }

    @Test
    void getTrack_whenNotFound_shouldThrow() {
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.getTrack(99L));
    }

    // --- getTrackWithCategories ---

    @Test
    void getTrackWithCategories_whenFound_withNullCategories_shouldReturnEmptyList() {
        Track track = track(1L, "Test Track", 10L);
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        TrackWithCategoriesDto result = trackService.getTrackWithCategories(1L);

        assertEquals(1L, result.getId());
        assertTrue(result.getCategories().isEmpty());
    }

    @Test
    void getTrackWithCategories_whenFound_withCategories_shouldMapThem() {
        Track track = track(1L, "Test Track", 10L);
        Category cat = Category.builder().name("Rock").build();
        setField(cat, "id", 50L);
        TrackCategory relation = TrackCategory.builder()
            .trackId(1L).categoryId(50L).category(cat).build();
        track.setCategoryRelations(List.of(relation));
        when(trackRepository.findById(1L)).thenReturn(Optional.of(track));

        TrackWithCategoriesDto result = trackService.getTrackWithCategories(1L);

        assertEquals(1, result.getCategories().size());
        assertEquals(50L, result.getCategories().get(0).getId());
        assertEquals("Rock", result.getCategories().get(0).getName());
    }

    @Test
    void getTrackWithCategories_whenNotFound_shouldThrow() {
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.getTrackWithCategories(99L));
    }

    // --- saveTrack ---

    @Test
    void saveTrack_create_shouldSaveNewTrack() {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder()
            .name("New Track").primaryArtistId(10L).build();
        Track saved = track(1L, "New Track", 10L);
        when(trackRepository.save(any(Track.class))).thenReturn(saved);

        TrackDto result = trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        assertEquals(1L, result.getId());
        assertEquals("New Track", result.getName());
        verify(trackRepository).save(any(Track.class));
    }

    @Test
    void saveTrack_create_withoutPrimaryArtistId_shouldThrow() {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder().name("New Track").build();

        assertThrows(IllegalArgumentException.class, () -> trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(trackRepository, never()).save(any());
    }

    @Test
    void saveTrack_update_whenFound_shouldUpdateAndSave() {
        Long trackId = 1L;
        Track existing = track(trackId, "Old Name", 10L);
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder()
            .id(trackId).name("New Name").build();
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(existing));
        when(trackRepository.save(existing)).thenReturn(existing);

        TrackDto result = trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        assertEquals("New Name", result.getName());
        assertEquals("New Name", existing.getName());
    }

    @Test
    void saveTrack_update_withNewPrimaryArtistId_shouldUpdateArtist() {
        Long trackId = 1L;
        Track existing = track(trackId, "Track", 10L);
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder()
            .id(trackId).name("Track").primaryArtistId(20L).build();
        when(trackRepository.findById(trackId)).thenReturn(Optional.of(existing));
        when(trackRepository.save(existing)).thenReturn(existing);

        trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        assertEquals(20L, existing.getPrimaryArtistId());
    }

    @Test
    void saveTrack_update_whenNotFound_shouldThrow() {
        TrackSaveRequestDTO request = TrackSaveRequestDTO.builder()
            .id(99L).name("Track").build();
        when(trackRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.saveTrack(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(trackRepository, never()).save(any());
    }

    // --- deleteTrack ---

    @Test
    void deleteTrack_whenExists_shouldDeleteAndReturnTrue() {
        when(trackRepository.existsById(1L)).thenReturn(true);

        boolean result = trackService.deleteTrack(1L);

        assertTrue(result);
        verify(trackRepository).deleteById(1L);
    }

    @Test
    void deleteTrack_whenNotExists_shouldReturnFalse() {
        when(trackRepository.existsById(1L)).thenReturn(false);

        boolean result = trackService.deleteTrack(1L);

        assertFalse(result);
        verify(trackRepository, never()).deleteById(any());
    }

    // --- bindToCategory ---

    @Test
    void bindToCategory_happyPath_shouldSaveRelation() {
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(trackCategoryRepository.existsByTrackIdAndCategoryId(1L, 2L)).thenReturn(false);

        trackService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        verify(trackCategoryRepository).save(any(TrackCategory.class));
    }

    @Test
    void bindToCategory_whenTrackNotFound_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(false);

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(trackCategoryRepository, never()).save(any());
    }

    @Test
    void bindToCategory_whenCategoryNotFound_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(false);

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(trackCategoryRepository, never()).save(any());
    }

    @Test
    void bindToCategory_whenRelationAlreadyExists_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(trackCategoryRepository.existsByTrackIdAndCategoryId(1L, 2L)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> trackService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(trackCategoryRepository, never()).save(any());
    }

    // --- unbindFromCategory ---

    @Test
    void unbindFromCategory_happyPath_shouldDeleteRelation() {
        TrackCategory relation = TrackCategory.builder().trackId(1L).categoryId(2L).build();
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(trackCategoryRepository.findByTrackIdAndCategoryId(1L, 2L)).thenReturn(Optional.of(relation));

        trackService.unbindFromCategory(1L, 2L);

        verify(trackCategoryRepository).delete(relation);
    }

    @Test
    void unbindFromCategory_whenTrackNotFound_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(false);

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.unbindFromCategory(1L, 2L));
        verify(trackCategoryRepository, never()).delete(any());
    }

    @Test
    void unbindFromCategory_whenCategoryNotFound_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(false);

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.unbindFromCategory(1L, 2L));
        verify(trackCategoryRepository, never()).delete(any());
    }

    @Test
    void unbindFromCategory_whenRelationNotFound_shouldThrow() {
        when(trackRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(trackCategoryRepository.findByTrackIdAndCategoryId(1L, 2L)).thenReturn(Optional.empty());

        assertThrows(CustomEntityNotFoundException.class, () -> trackService.unbindFromCategory(1L, 2L));
        verify(trackCategoryRepository, never()).delete(any());
    }

    // --- helpers ---

    private Track track(Long id, String name, Long primaryArtistId) {
        Track track = Track.builder().name(name).primaryArtistId(primaryArtistId).build();
        setField(track, "id", id);
        return track;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            Class<?> clazz = target.getClass();
            while (clazz != null) {
                try {
                    var field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.set(target, value);
                    return;
                } catch (NoSuchFieldException e) {
                    clazz = clazz.getSuperclass();
                }
            }
            throw new NoSuchFieldException(fieldName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }
}
