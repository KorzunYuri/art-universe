package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.approved.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.DimensionRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryBindingRepository categoryBindingRepository;

    @Mock
    private DimensionRepository dimensionRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    @Test
    void findBoundCategories_shouldReturnListOfBoundCategories() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Rock"
        );
        List<BoundEntityProjection> expectedResult = List.of(projection);
        
        when(categoryBindingRepository.findBoundCategoriesForDataSource(dataSource, externalIds))
            .thenReturn(expectedResult);

        // When
        List<BoundEntityProjection> result = categoryService.findBoundCategories(dataSource, externalIds);

        // Then
        assertEquals(expectedResult, result);
        verify(categoryBindingRepository).findBoundCategoriesForDataSource(dataSource, externalIds);
    }

    @Test
    void findCategory_shouldReturnSingleBoundCategory() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, "Rock"
        );
        
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryService.findCategory(dataSource, externalId);

        // Then
        assertEquals(projection, result);
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void findCategory_whenNotFound_shouldReturnNull() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(null);

        // When
        BoundEntityProjection result = categoryService.findCategory(dataSource, externalId);

        // Then
        assertNull(result);
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void bindToExisting_whenCategoryExists_shouldCreateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        
        Category existingCategory = Category.builder()
            .id(categoryId)
            .name("Rock")
            .build();
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        CategoryBinding binding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(categoryId)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, categoryId, "Rock"
        );
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(categoryBindingRepository.save(any(CategoryBinding.class))).thenReturn(binding);
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = categoryService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).save(any(CategoryBinding.class));
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void bindToExisting_whenCategoryDoesNotExist_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(EntityNotFoundException.class, () -> 
            categoryService.bindToExisting(dataSource, externalId, request));
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryBindingRepository, never()).save(any());
    }

    @Test
    void bindToExisting_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        Long oldCategoryId = 102L;
        
        Category existingCategory = Category.builder()
            .id(categoryId)
            .name("Rock")
            .build();
        
        CategoryBinding existingBinding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(oldCategoryId)
            .build();
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, categoryId, "Rock"
        );
        
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(existingCategory));
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(categoryBindingRepository.save(existingBinding)).thenReturn(existingBinding);
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = categoryService.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        assertEquals(categoryId, existingBinding.getReferenceId());
        
        verify(categoryRepository).findById(categoryId);
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).save(existingBinding);
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void createAndBind_shouldCreateCategoryAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        
        Category newCategory = Category.builder()
            .id(101L)
            .name(categoryName)
            .build();
        
        CategoryCreateAndBindRequestDTO request = CategoryCreateAndBindRequestDTO.builder()
            .name(categoryName)
            .build();
        
        CategoryBinding binding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(newCategory.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newCategory.getId(), categoryName
        );
        
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());
        when(categoryBindingRepository.save(any(CategoryBinding.class))).thenReturn(binding);
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = categoryService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        
        verify(categoryRepository).save(any(Category.class));
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).save(any(CategoryBinding.class));
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void createAndBind_whenBindingExists_shouldUpdateBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        
        Category newCategory = Category.builder()
            .id(101L)
            .name(categoryName)
            .build();
        
        CategoryBinding existingBinding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(999L) // Old reference
            .build();
        
        CategoryCreateAndBindRequestDTO request = CategoryCreateAndBindRequestDTO.builder()
            .name(categoryName)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newCategory.getId(), categoryName
        );
        
        when(categoryRepository.save(any(Category.class))).thenReturn(newCategory);
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        when(categoryBindingRepository.save(existingBinding)).thenReturn(existingBinding);
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(expectedResult);

        // When
        BoundEntityProjection result = categoryService.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(expectedResult, result);
        assertEquals(newCategory.getId(), existingBinding.getReferenceId());
        
        verify(categoryRepository).save(any(Category.class));
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).save(existingBinding);
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void unbindCategory_whenBindingExists_shouldDeleteBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        CategoryBinding existingBinding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .referenceId(101L)
            .build();
        
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.of(existingBinding));
        doNothing().when(categoryBindingRepository).delete(existingBinding);

        // When
        boolean result = categoryService.unbindCategory(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).delete(existingBinding);
    }

    @Test
    void unbindCategory_whenBindingDoesNotExist_shouldReturnFalse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(categoryBindingRepository.findByDataSourceAndExternalId(dataSource, externalId))
            .thenReturn(Optional.empty());

        // When
        boolean result = categoryService.unbindCategory(dataSource, externalId);

        // Then
        assertFalse(result);
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository, never()).delete(any());
    }

    @Test
    void lookupCategories_shouldReturnMatchingCategories() {
        // Given
        String searchTerm = "rock";
        Category category1 = Category.builder().id(1L).name("Rock").build();
        Category category2 = Category.builder().id(2L).name("Alternative Rock").build();
        List<Category> categories = List.of(category1, category2);
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, 20))
            .thenReturn(categories);
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Rock", result.get(0).getName());
        assertEquals(2L, result.get(1).getId());
        assertEquals("Alternative Rock", result.get(1).getName());
        
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, 20);
    }

    @Test
    void lookupCategories_withLimit_shouldReturnLimitedResults() {
        // Given
        String searchTerm = "band";
        int limit = 3;
        
        // Create 5 categories
        List<Category> categories = IntStream.rangeClosed(1, 5)
            .mapToObj(i -> Category.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, limit))
            .thenReturn(categories.subList(0, limit));
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm, limit);
        
        // Then
        assertEquals(limit, result.size());
        for (int i = 0; i < limit; i++) {
            assertEquals((long) (i + 1), result.get(i).getId());
            assertEquals("Band " + (i + 1), result.get(i).getName());
        }
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, limit);
    }

    @Test
    void lookupCategories_withNullLimit_shouldUseDefaultLimit() {
        // Given
        String searchTerm = "band";
        Integer limit = null;
        int defaultLimit = 20;
        
        // Create 30 categories (more than default limit of 20)
        List<Category> categories = IntStream.rangeClosed(1, 30)
            .mapToObj(i -> Category.builder().id((long) i).name("Band " + i).build())
            .collect(Collectors.toList());
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, defaultLimit))
            .thenReturn(categories.subList(0, defaultLimit));
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm, limit);
        
        // Then
        assertEquals(defaultLimit, result.size());
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, defaultLimit);
    }

    @Test
    void lookupCategories_withEmptySearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "";
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_withNullSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = null;
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_withWhitespaceSearchTerm_shouldReturnEmptyList() {
        // Given
        String searchTerm = "   ";
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(any(), anyInt());
    }

    @Test
    void lookupCategories_shouldTrimSearchTerm() {
        // Given
        String searchTerm = "  rock  ";
        String trimmedSearchTerm = "rock";
        Category category = Category.builder().id(1L).name("Rock").build();
        List<Category> categories = List.of(category);
        
        when(categoryRepository.findByNameContainingIgnoreCase(trimmedSearchTerm, 20))
            .thenReturn(categories);
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertEquals(1, result.size());
        assertEquals("Rock", result.get(0).getName());
        
        verify(categoryRepository).findByNameContainingIgnoreCase(trimmedSearchTerm, 20);
    }

    @Test
    void lookupCategories_withNoMatches_shouldReturnEmptyList() {
        // Given
        String searchTerm = "nonexistent";
        
        when(categoryRepository.findByNameContainingIgnoreCase(searchTerm, 20))
            .thenReturn(List.of());
        
        // When
        List<LookupResultDTO> result = categoryService.lookupCategories(searchTerm);
        
        // Then
        assertTrue(result.isEmpty());
        verify(categoryRepository).findByNameContainingIgnoreCase(searchTerm, 20);
    }

    @Test
    void saveCategory_whenCategoryIsParentOfItself_shouldThrowException() {
        // Given
        Long categoryId = 1L;
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(categoryId)
            .name("Test Category")
            .parentId(categoryId) // Same as ID - self-parent
            .build();

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            categoryService.saveCategory(request));
        
        assertEquals("Category cannot be parent of itself", exception.getMessage());
        
        // Verify no repository calls were made
        verify(categoryRepository, never()).save(any());
        verify(dimensionRepository, never()).findById(any());
    }
}
