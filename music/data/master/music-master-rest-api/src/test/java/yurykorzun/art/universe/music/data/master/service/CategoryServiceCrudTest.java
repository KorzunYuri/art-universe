package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryWithParentsDto;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryCategory;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryCategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceCrudTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private CategoryBindingRepository categoryBindingRepository;
    @Mock private CategoryCategoryRepository categoryCategoryRepository;
    @Mock private EntityManager entityManager;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    // --- findCategories ---

    @Test
    void findCategories_shouldDelegateToRepository() {
        String search = "rock";
        Pageable pageable = PageRequest.of(0, 10);
        Category cat = category(1L, "Rock");
        when(categoryRepository.findCategories(search, pageable))
            .thenReturn(new PageImpl<>(List.of(cat)));

        Page<CategoryDto> result = categoryService.findCategories(search, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Rock", result.getContent().get(0).getName());
    }

    // --- findCategoriesWithParents ---

    @Test
    void findCategoriesWithParents_whenEmpty_shouldReturnEmpty() {
        String search = "nonexistent";
        Pageable pageable = PageRequest.of(0, 10);
        when(categoryRepository.findCategoriesWithParentsEntities(search, pageable))
            .thenReturn(new PageImpl<>(List.of()));

        Page<CategoryWithParentsDto> result = categoryService.findCategoriesWithParents(search, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void findCategoriesWithParents_withResults_shouldMapCorrectly() {
        String search = "rock";
        Pageable pageable = PageRequest.of(0, 10);
        Category cat = category(1L, "Rock");
        when(categoryRepository.findCategoriesWithParentsEntities(search, pageable))
            .thenReturn(new PageImpl<>(List.of(cat)));
        when(categoryRepository.findCategoriesWithParentsByIds(List.of(1L)))
            .thenReturn(List.of(cat));

        Page<CategoryWithParentsDto> result = categoryService.findCategoriesWithParents(search, pageable);

        assertEquals(1, result.getContent().size());
        assertEquals("Rock", result.getContent().get(0).getName());
    }

    // --- getCategory ---

    @Test
    void getCategory_whenFound_shouldReturnDto() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category(1L, "Rock")));

        CategoryDto result = categoryService.getCategory(1L);

        assertEquals(1L, result.getId());
        assertEquals("Rock", result.getName());
    }

    @Test
    void getCategory_whenNotFound_shouldThrow() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.getCategory(99L));
    }

    // --- saveCategory ---

    @Test
    void saveCategory_create_shouldSaveNewCategory() {
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("New Category").build();
        Category saved = category(1L, "New Category");
        when(categoryRepository.save(any(Category.class))).thenReturn(saved);

        CategoryDto result = categoryService.saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        assertEquals(1L, result.getId());
        assertEquals("New Category", result.getName());
    }

    @Test
    void saveCategory_update_whenFound_shouldUpdateAndSave() {
        Category existing = category(1L, "Old Name");
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(1L).name("New Name").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepository.save(existing)).thenReturn(existing);

        CategoryDto result = categoryService.saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

        assertEquals("New Name", result.getName());
    }

    @Test
    void saveCategory_update_whenNotFound_shouldThrow() {
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(99L).name("Name").build();
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> categoryService.saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        verify(categoryRepository, never()).save(any());
    }

    // --- deleteCategory ---

    @Test
    void deleteCategory_whenExists_noChildrenNoParents_shouldDeleteAndReturnTrue() {
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryCategoryRepository.findParentIds(1L)).thenReturn(List.of());
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of());

        boolean result = categoryService.deleteCategory(1L);

        assertTrue(result);
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_whenNotExists_shouldReturnFalse() {
        when(categoryRepository.existsById(1L)).thenReturn(false);

        boolean result = categoryService.deleteCategory(1L);

        assertFalse(result);
        verify(categoryRepository, never()).deleteById(any());
    }

    @Test
    void deleteCategory_withParentsAndChildren_shouldCascadeRelations() {
        // Given: parent(10) -> category(1) -> child(20)
        // Deleting 1 should create parent(10) -> child(20)
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryCategoryRepository.findParentIds(1L)).thenReturn(List.of(10L));
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(20L));
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(10L, 20L)).thenReturn(false);
        // No cycle: path from child to parent does not exist
        when(categoryCategoryRepository.findChildIds(20L)).thenReturn(List.of());

        boolean result = categoryService.deleteCategory(1L);

        assertTrue(result);
        verify(categoryCategoryRepository).save(any(CategoryCategory.class));
        verify(categoryRepository).deleteById(1L);
    }

    @Test
    void deleteCategory_withExistingCascadeRelation_shouldSkipCreation() {
        // Given: parent(10) -> category(1) -> child(20), and parent(10) -> child(20) already exists
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(categoryCategoryRepository.findParentIds(1L)).thenReturn(List.of(10L));
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(20L));
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(10L, 20L)).thenReturn(true);

        boolean result = categoryService.deleteCategory(1L);

        assertTrue(result);
        verify(categoryCategoryRepository, never()).save(any());
        verify(categoryRepository).deleteById(1L);
    }

    // --- helpers ---

    private Category category(Long id, String name) {
        Category cat = Category.builder().name(name).build();
        setField(cat, "id", id);
        return cat;
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
