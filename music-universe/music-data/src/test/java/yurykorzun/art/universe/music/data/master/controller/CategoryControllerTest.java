package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.TestCategoryHierarchyProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.CategoryService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void searchCategories_shouldReturnPageOfCategories() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        CategoryHierarchyProjection category1 = new TestCategoryHierarchyProjectionImpl(
            1L, "Genre", 1L, 1L, null, 1, "Dimension 1", "Dimension 1", null);
        CategoryHierarchyProjection category2 = new TestCategoryHierarchyProjectionImpl(
            2L, "Subgenre", 1L, 1L, 1L, 2, "Dimension 1", "Dimension 1", "Genre");
        
        List<CategoryHierarchyProjection> categories = Arrays.asList(category1, category2);
        Page<CategoryHierarchyProjection> expectedPage = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryService.searchCategories(search, pageable)).thenReturn(expectedPage);

        // When
        Page<CategoryHierarchyProjection> result = categoryController.searchCategories(search, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(categoryService).searchCategories(search, pageable);
    }

    @Test
    void searchCategories_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Test error";
        
        when(categoryService.searchCategories(search, pageable)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.searchCategories(search, pageable)
        );
        
        assertEquals("Failed to search categories: " + errorMessage, exception.getMessage());
        verify(categoryService).searchCategories(search, pageable);
    }

    @Test
    void lookupCategories_shouldReturnListOfLookupResults() {
        // Given
        String name = "rock";
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(name)).thenReturn(expectedCategories);

        // When
        List<LookupResultDTO> result = categoryController.lookupCategories(name, null);

        // Then
        assertEquals(expectedCategories, result);
        verify(categoryService).lookupCategories(name);
    }

    @Test
    void lookupCategories_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String name = "rock";
        Integer limit = 5;
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(name, limit)).thenReturn(expectedCategories);

        // When
        List<LookupResultDTO> result = categoryController.lookupCategories(name, limit);

        // Then
        assertEquals(expectedCategories, result);
        verify(categoryService).lookupCategories(name, limit);
    }

    @Test
    void lookupCategories_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        String name = "rock";
        String errorMessage = "Test error";
        
        when(categoryService.lookupCategories(name)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.lookupCategories(name, null)
        );
        
        assertEquals("Failed to lookup categories: " + errorMessage, exception.getMessage());
        verify(categoryService).lookupCategories(name);
    }

    @Test
    void saveCategory_shouldReturnCategoryHierarchyProjection() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .dimensionId(1L)
            .build();
        
        CategoryHierarchyProjection savedCategory = new TestCategoryHierarchyProjectionImpl(
            1L, "Genre", 1L, 1L, null, 1, "Dimension 1", "Dimension 1", null);
        
        when(categoryService.saveCategory(request)).thenReturn(savedCategory);

        // When
        CategoryHierarchyProjection result = categoryController.saveCategory(request);

        // Then
        assertEquals(savedCategory, result);
        verify(categoryService).saveCategory(request);
    }

    @Test
    void saveCategory_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .dimensionId(1L)
            .build();
        String errorMessage = "Test error";
        
        when(categoryService.saveCategory(request)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.saveCategory(request)
        );
        
        assertEquals("Failed to save category: " + errorMessage, exception.getMessage());
        verify(categoryService).saveCategory(request);
    }

    @Test
    void saveCategory_whenSelfParentValidationFails_shouldThrowDataAccessException() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(1L)
            .name("Test Category")
            .parentId(1L) // Same as ID - self-parent
            .build();
        String errorMessage = "Category cannot be parent of itself";
        
        when(categoryService.saveCategory(request)).thenThrow(new IllegalArgumentException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.saveCategory(request)
        );
        
        assertEquals("Failed to save category: " + errorMessage, exception.getMessage());
        verify(categoryService).saveCategory(request);
    }

    @Test
    void deleteCategory_whenFound_shouldReturnTrue() {
        // Given
        Long id = 1L;
        
        when(categoryService.deleteCategory(id)).thenReturn(true);

        // When
        boolean result = categoryController.deleteCategory(id);

        // Then
        assertTrue(result);
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void deleteCategory_whenNotFound_shouldThrowEntityNotFoundException() {
        // Given
        Long id = 1L;
        
        when(categoryService.deleteCategory(id)).thenReturn(false);

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () -> 
            categoryController.deleteCategory(id)
        );
        
        assertEquals("Category not found with id: " + id, exception.getMessage());
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void deleteCategory_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        Long id = 1L;
        String errorMessage = "Test error";
        
        when(categoryService.deleteCategory(id)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.deleteCategory(id)
        );
        
        assertEquals("Failed to delete category: " + errorMessage, exception.getMessage());
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void findBoundCategories_shouldReturnListOfBoundEntityProjections() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Rock"
        );
        List<BoundEntityProjection> expectedBindings = List.of(projection);
        
        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenReturn(expectedBindings);

        // When
        List<BoundEntityProjection> result = categoryController.findBoundCategories(dataSource, externalIds);

        // Then
        assertEquals(expectedBindings, result);
        verify(categoryService).findBoundCategories(dataSource, externalIds);
    }

    @Test
    void findBoundCategories_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        String errorMessage = "Test error";
        
        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.findBoundCategories(dataSource, externalIds)
        );
        
        assertEquals("Failed to get bound categories: " + errorMessage, exception.getMessage());
        verify(categoryService).findBoundCategories(dataSource, externalIds);
    }

    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, categoryId, "Rock"
        );
        
        when(categoryService.bindToExisting(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(categoryService).bindToExisting(dataSource, externalId, request);
    }

    @Test
    void bindToExisting_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        String errorMessage = "Test error";
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        when(categoryService.bindToExisting(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            categoryController.bindToExisting(dataSource, externalId, request)
        );
        
        assertEquals("Failed to bind category to existing: " + errorMessage, exception.getMessage());
        verify(categoryService).bindToExisting(dataSource, externalId, request);
    }

    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        
        CategoryCreateAndBindRequestDTO request = CategoryCreateAndBindRequestDTO.builder()
            .name(categoryName)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, categoryName
        );
        
        when(categoryService.createAndBind(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(categoryService).createAndBind(dataSource, externalId, request);
    }

    @Test
    void createAndBind_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        String errorMessage = "Test error";
        
        CategoryCreateAndBindRequestDTO request = CategoryCreateAndBindRequestDTO.builder()
            .name(categoryName)
            .build();
        
        when(categoryService.createAndBind(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            categoryController.createAndBind(dataSource, externalId, request)
        );
        
        assertEquals("Failed to create and bind category: " + errorMessage, exception.getMessage());
        verify(categoryService).createAndBind(dataSource, externalId, request);
    }

    @Test
    void unbindCategory_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(categoryService.unbindCategory(dataSource, externalId)).thenReturn(true);

        // When
        boolean result = categoryController.unbindCategory(dataSource, externalId);

        // Then
        assertTrue(result);
        verify(categoryService).unbindCategory(dataSource, externalId);
    }

    @Test
    void unbindCategory_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(categoryService.unbindCategory(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            categoryController.unbindCategory(dataSource, externalId)
        );
        
        assertEquals("Failed to unbind category: " + errorMessage, exception.getMessage());
        verify(categoryService).unbindCategory(dataSource, externalId);
    }
    
    @Test
    void batchLookupCategories_shouldReturnCategoryBatchLookupResponseDTO() {
        // Given
        List<String> names = List.of("rock", "jazz");
        Integer limit = 10;
        
        CategoryBatchLookupRequestDTO request = CategoryBatchLookupRequestDTO.builder()
            .names(names)
            .limit(limit)
            .build();
        
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        resultMap.put("rock", List.of(
            new LookupResultDTO(1L, "Rock"),
            new LookupResultDTO(2L, "Alternative Rock")
        ));
        resultMap.put("jazz", List.of(
            new LookupResultDTO(3L, "Jazz")
        ));
        
        CategoryBatchLookupResponseDTO expectedResponse = CategoryBatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(categoryService.batchLookupCategories(request)).thenReturn(expectedResponse);
        
        // When
        CategoryBatchLookupResponseDTO result = categoryController.batchLookupCategories(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(categoryService).batchLookupCategories(request);
    }
    
    @Test
    void batchLookupCategories_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        List<String> names = List.of("rock", "jazz");
        Integer limit = 10;
        String errorMessage = "Test error";
        
        CategoryBatchLookupRequestDTO request = CategoryBatchLookupRequestDTO.builder()
            .names(names)
            .limit(limit)
            .build();
        
        when(categoryService.batchLookupCategories(request))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            categoryController.batchLookupCategories(request)
        );
        
        assertEquals("Failed to batch lookup categories: " + errorMessage, exception.getMessage());
        verify(categoryService).batchLookupCategories(request);
    }
}
