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
import yurykorzun.art.universe.music.data.master.dto.AlbumDto;
import yurykorzun.art.universe.music.data.master.dto.AlbumSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.AlbumWithCategoriesDto;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.repository.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlbumServiceCrudTest {

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

    @InjectMocks
    private AlbumServiceImpl albumService;

    // --- findAlbums ---

    @Test
    void findAlbums_withoutCategoryId_shouldCallRepositoryWithoutCategory() {
        // Given
        String search = "abbey";
        Pageable pageable = PageRequest.of(0, 10);
        Album album = album(1L, "Abbey Road", 10L);
        when(albumRepository.findAlbums(search, pageable))
            .thenReturn(new PageImpl<>(List.of(album)));

        // When
        Page<AlbumDto> result = albumService.findAlbums(search, null, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Abbey Road", result.getContent().get(0).getName());
        verify(albumRepository).findAlbums(search, pageable);
        verify(albumRepository, never()).findAlbums(anyString(), anyLong(), any());
    }

    @Test
    void findAlbums_withCategoryId_shouldCallRepositoryWithCategory() {
        // Given
        String search = "abbey";
        Long categoryId = 5L;
        Pageable pageable = PageRequest.of(0, 10);
        when(albumRepository.findAlbums(search, categoryId, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        // When
        albumService.findAlbums(search, categoryId, pageable);

        // Then
        verify(albumRepository).findAlbums(search, categoryId, pageable);
        verify(albumRepository, never()).findAlbums(anyString(), any(Pageable.class));
    }

    // --- findAlbumsWithCategories ---

    @Test
    void findAlbumsWithCategories_shouldDelegateToRepository() {
        // Given
        String search = "abbey";
        Pageable pageable = PageRequest.of(0, 10);
        Album album = album(1L, "Abbey Road", 10L);
        when(albumRepository.findAlbumsWithCategories(search, null, pageable))
            .thenReturn(new PageImpl<>(List.of(album)));

        // When
        Page<AlbumWithCategoriesDto> result = albumService.findAlbumsWithCategories(search, null, pageable);

        // Then
        assertEquals(1, result.getContent().size());
        assertEquals("Abbey Road", result.getContent().get(0).getName());
        verify(albumRepository).findAlbumsWithCategories(search, null, pageable);
    }

    // --- getAlbum ---

    @Test
    void getAlbum_whenFound_shouldReturnDto() {
        // Given
        Long id = 1L;
        when(albumRepository.findById(id)).thenReturn(Optional.of(album(id, "Abbey Road", 10L)));

        // When
        AlbumDto result = albumService.getAlbum(id);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Abbey Road", result.getName());
        assertEquals(10L, result.getPrimaryArtistId());
    }

    @Test
    void getAlbum_whenNotFound_shouldThrow() {
        // Given
        when(albumRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.getAlbum(99L));
    }

    // --- getAlbumWithCategories ---

    @Test
    void getAlbumWithCategories_whenFound_withNullCategories_shouldReturnEmptyList() {
        // Given
        Album album = album(1L, "Abbey Road", 10L);
        // categoryRelations is null by default
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // When
        AlbumWithCategoriesDto result = albumService.getAlbumWithCategories(1L);

        // Then
        assertEquals(1L, result.getId());
        assertTrue(result.getCategories().isEmpty());
    }

    @Test
    void getAlbumWithCategories_whenFound_withCategories_shouldMapThem() {
        // Given
        Album album = album(1L, "Abbey Road", 10L);
        Category cat = Category.builder().name("Rock").build();
        setField(cat, "id", 50L);
        AlbumCategory relation = AlbumCategory.builder()
            .albumId(1L).categoryId(50L).category(cat).build();
        album.setCategoryRelations(List.of(relation));
        when(albumRepository.findById(1L)).thenReturn(Optional.of(album));

        // When
        AlbumWithCategoriesDto result = albumService.getAlbumWithCategories(1L);

        // Then
        assertEquals(1, result.getCategories().size());
        assertEquals(50L, result.getCategories().get(0).getId());
        assertEquals("Rock", result.getCategories().get(0).getName());
    }

    @Test
    void getAlbumWithCategories_whenNotFound_shouldThrow() {
        // Given
        when(albumRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.getAlbumWithCategories(99L));
    }

    // --- saveAlbum ---

    @Test
    void saveAlbum_create_shouldSaveNewAlbum() {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .name("New Album").primaryArtistId(10L).build();
        Album saved = album(1L, "New Album", 10L);
        when(albumRepository.save(any(Album.class))).thenReturn(saved);

        // When
        AlbumDto result = albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        assertEquals(1L, result.getId());
        assertEquals("New Album", result.getName());
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    void saveAlbum_create_withoutPrimaryArtistId_shouldThrow() {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder().name("New Album").build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumRepository, never()).save(any());
    }

    @Test
    void saveAlbum_update_whenFound_shouldUpdateAndSave() {
        // Given
        Long albumId = 1L;
        Album existing = album(albumId, "Old Name", 10L);
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .id(albumId).name("New Name").build();
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existing));
        when(albumRepository.save(existing)).thenReturn(existing);

        // When
        AlbumDto result = albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        assertEquals("New Name", result.getName());
        assertEquals("New Name", existing.getName());
        verify(albumRepository).findById(albumId);
        verify(albumRepository).save(existing);
    }

    @Test
    void saveAlbum_update_withNewPrimaryArtistId_shouldUpdateArtist() {
        // Given
        Long albumId = 1L;
        Album existing = album(albumId, "Album", 10L);
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .id(albumId).name("Album").primaryArtistId(20L).build();
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(existing));
        when(albumRepository.save(existing)).thenReturn(existing);

        // When
        albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        assertEquals(20L, existing.getPrimaryArtistId());
    }

    @Test
    void saveAlbum_update_whenNotFound_shouldThrow() {
        // Given
        AlbumSaveRequestDTO request = AlbumSaveRequestDTO.builder()
            .id(99L).name("Album").build();
        when(albumRepository.findById(99L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.saveAlbum(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumRepository, never()).save(any());
    }

    // --- deleteAlbum ---

    @Test
    void deleteAlbum_whenExists_shouldDeleteAndReturnTrue() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);

        // When
        boolean result = albumService.deleteAlbum(1L);

        // Then
        assertTrue(result);
        verify(albumRepository).deleteById(1L);
    }

    @Test
    void deleteAlbum_whenNotExists_shouldReturnFalse() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(false);

        // When
        boolean result = albumService.deleteAlbum(1L);

        // Then
        assertFalse(result);
        verify(albumRepository, never()).deleteById(any());
    }

    // --- bindToCategory ---

    @Test
    void bindToCategory_happyPath_shouldSaveRelation() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(albumCategoryRepository.existsByAlbumIdAndCategoryId(1L, 2L)).thenReturn(false);

        // When
        albumService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        // Then
        verify(albumCategoryRepository).save(any(AlbumCategory.class));
    }

    @Test
    void bindToCategory_whenAlbumNotFound_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumCategoryRepository, never()).save(any());
    }

    @Test
    void bindToCategory_whenCategoryNotFound_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumCategoryRepository, never()).save(any());
    }

    @Test
    void bindToCategory_whenRelationAlreadyExists_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(albumCategoryRepository.existsByAlbumIdAndCategoryId(1L, 2L)).thenReturn(true);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> albumService.bindToCategory(1L, 2L, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(albumCategoryRepository, never()).save(any());
    }

    // --- unbindFromCategory ---

    @Test
    void unbindFromCategory_happyPath_shouldDeleteRelation() {
        // Given
        AlbumCategory relation = AlbumCategory.builder().albumId(1L).categoryId(2L).build();
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(albumCategoryRepository.findByAlbumIdAndCategoryId(1L, 2L)).thenReturn(Optional.of(relation));

        // When
        albumService.unbindFromCategory(1L, 2L);

        // Then
        verify(albumCategoryRepository).delete(relation);
    }

    @Test
    void unbindFromCategory_whenAlbumNotFound_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.unbindFromCategory(1L, 2L));
        verify(albumCategoryRepository, never()).delete(any());
    }

    @Test
    void unbindFromCategory_whenCategoryNotFound_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(false);

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.unbindFromCategory(1L, 2L));
        verify(albumCategoryRepository, never()).delete(any());
    }

    @Test
    void unbindFromCategory_whenRelationNotFound_shouldThrow() {
        // Given
        when(albumRepository.existsById(1L)).thenReturn(true);
        when(categoryRepository.existsById(2L)).thenReturn(true);
        when(albumCategoryRepository.findByAlbumIdAndCategoryId(1L, 2L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () -> albumService.unbindFromCategory(1L, 2L));
        verify(albumCategoryRepository, never()).delete(any());
    }

    // --- unbindAlbum ---

    @Test
    void unbindAlbum_whenBindingExists_shouldDeleteAndReturnTrue() {
        // Given
        AlbumBinding binding = AlbumBinding.builder()
            .dataSource(DataSource.LASTFM).externalId(100L).masterId(1L).build();
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, 100L))
            .thenReturn(Optional.of(binding));

        // When
        boolean result = albumService.unbindAlbum(DataSource.LASTFM, 100L);

        // Then
        assertTrue(result);
        verify(albumBindingRepository).delete(binding);
    }

    @Test
    void unbindAlbum_whenBindingNotExists_shouldReturnFalse() {
        // Given
        when(albumBindingRepository.findByDataSourceAndExternalId(DataSource.LASTFM, 100L))
            .thenReturn(Optional.empty());

        // When
        boolean result = albumService.unbindAlbum(DataSource.LASTFM, 100L);

        // Then
        assertFalse(result);
        verify(albumBindingRepository, never()).delete(any());
    }

    // --- bindToExisting ---

    @Test
    void bindToExisting_shouldThrowUnsupportedOperation() {
        // When & Then
        assertThrows(UnsupportedOperationException.class,
            () -> albumService.bindToExisting(DataSource.LASTFM, 1L, null, Origin.MANUAL, MasterApprovalStatus.APPROVED));
    }

    // --- helpers ---

    private Album album(Long id, String name, Long primaryArtistId) {
        Album album = Album.builder().name(name).primaryArtistId(primaryArtistId).build();
        setField(album, "id", id);
        return album;
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            // Walk up the class hierarchy to find the field
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
