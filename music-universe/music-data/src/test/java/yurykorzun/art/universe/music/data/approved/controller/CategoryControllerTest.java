package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.TestCategoryHierarchyProjectionImpl;
import yurykorzun.art.universe.music.data.approved.service.CategoryService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void searchCategories_shouldReturnSuccessResponse() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        CategoryHierarchyProjection category1 = new TestCategoryHierarchyProjectionImpl(
            1L, "Genre", 1L, 1L, null, 1, "Dimension 1", "Dimension 1", null);
        CategoryHierarchyProjection category2 = new TestCategoryHierarchyProjectionImpl(
            2L, "Subgenre", 1L, 1L, 1L, 2, "Dimension 1", "Dimension 1", "Genre");
        
        List<CategoryHierarchyProjection> categories = Arrays.asList(category1, category2);
        Page<CategoryHierarchyProjection> page = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryService.searchCategories(search, pageable)).thenReturn(page);
        ResponseEntity<ResponseWrapper<Page<CategoryHierarchyProjection>>> expectedResponse = 
            ResponseWrapper.success(page);

        // When
        ResponseEntity<ResponseWrapper<Page<CategoryHierarchyProjection>>> actualResponse = 
            categoryController.searchCategories(search, pageable);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).searchCategories(search, pageable);
    }

    @Test
    void searchCategories_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        String errorMessage = "Test error";
        
        when(categoryService.searchCategories(search, pageable)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Page<CategoryHierarchyProjection>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to search categories: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Page<CategoryHierarchyProjection>>> actualResponse = 
            categoryController.searchCategories(search, pageable);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).searchCategories(search, pageable);
    }

    @Test
    void saveCategory_shouldReturnSuccessResponse() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .dimensionId(1L)
            .build();
        
        CategoryHierarchyProjection savedCategory = new TestCategoryHierarchyProjectionImpl(
            1L, "Genre", 1L, 1L, null, 1, "Dimension 1", "Dimension 1", null);
        
        when(categoryService.saveCategory(request)).thenReturn(savedCategory);
        ResponseEntity<ResponseWrapper<CategoryHierarchyProjection>> expectedResponse = 
            ResponseWrapper.success(savedCategory);

        // When
        ResponseEntity<ResponseWrapper<CategoryHierarchyProjection>> actualResponse = 
            categoryController.saveCategory(request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).saveCategory(request);
    }

    @Test
    void saveCategory_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .dimensionId(1L)
            .build();
        String errorMessage = "Test error";
        
        when(categoryService.saveCategory(request)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<CategoryHierarchyProjection>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to save category: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<CategoryHierarchyProjection>> actualResponse = 
            categoryController.saveCategory(request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).saveCategory(request);
    }

    @Test
    void deleteCategory_whenFound_shouldReturnSuccessResponse() {
        // Given
        Long id = 1L;
        
        when(categoryService.deleteCategory(id)).thenReturn(true);
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = ResponseWrapper.success(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse = categoryController.deleteCategory(id);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void deleteCategory_whenNotFound_shouldReturnFailureResponse() {
        // Given
        Long id = 1L;
        
        when(categoryService.deleteCategory(id)).thenReturn(false);
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = 
            ResponseWrapper.failure("Category not found with id: " + id);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse = categoryController.deleteCategory(id);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void deleteCategory_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        Long id = 1L;
        String errorMessage = "Test error";
        
        when(categoryService.deleteCategory(id)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to delete category: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse = categoryController.deleteCategory(id);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).deleteCategory(id);
    }
}
