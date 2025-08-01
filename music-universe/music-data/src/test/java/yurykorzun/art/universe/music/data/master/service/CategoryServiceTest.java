package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryBindingRepository categoryBindingRepository;

    @Mock
    private DimensionRepository dimensionRepository;
    
    @Mock
    private EntityManager entityManager;
    
    @Mock
    private Query query;

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
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(categoryId)
            .build();
        
        CategoryBinding binding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(categoryId)
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
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(categoryId)
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
            .masterId(oldCategoryId)
            .build();
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(categoryId)
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
        assertEquals(categoryId, existingBinding.getMasterId());
        
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
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(categoryName)
            .build();
        
        CategoryBinding binding = CategoryBinding.builder()
            .id(1L)
            .dataSource(dataSource)
            .externalId(externalId)
            .masterId(newCategory.getId())
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newCategory.getId(), categoryName
        );
        
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());
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
        
        verify(categoryRepository).findByName(categoryName);
        verify(categoryRepository).save(any(Category.class));
        verify(categoryBindingRepository).findByDataSourceAndExternalId(dataSource, externalId);
        verify(categoryBindingRepository).save(any(CategoryBinding.class));
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void createAndBind_whenCategoryWithNameExists_shouldThrowException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "Existing Category";
        
        Category existingCategory = Category.builder()
            .id(999L)
            .name(categoryName)
            .build();
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(categoryName)
            .build();
        
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.of(existingCategory));

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            categoryService.createAndBind(dataSource, externalId, request));
        
        assertEquals("Category with name Existing Category already exists", exception.getMessage());
        
        verify(categoryRepository).findByName(categoryName);
        verify(categoryRepository, never()).save(any(Category.class));
        verify(categoryBindingRepository, never()).save(any(CategoryBinding.class));
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
            .masterId(999L) // Old reference
            .build();
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(categoryName)
            .build();
        
        TestBoundEntityProjectionImpl expectedResult = new TestBoundEntityProjectionImpl(
            externalId, dataSource, newCategory.getId(), categoryName
        );
        
        when(categoryRepository.findByName(categoryName)).thenReturn(Optional.empty());
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
        assertEquals(newCategory.getId(), existingBinding.getMasterId());
        
        verify(categoryRepository).findByName(categoryName);
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
            .masterId(101L)
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
