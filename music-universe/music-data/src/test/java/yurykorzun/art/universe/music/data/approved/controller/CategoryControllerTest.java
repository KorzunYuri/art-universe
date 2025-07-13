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
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.CategoryBatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestCategoryHierarchyProjectionImpl;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.service.CategoryService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    void lookupCategories_shouldReturnSuccessResponse() {
        // Given
        String name = "rock";
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(name)).thenReturn(expectedCategories);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.success(expectedCategories);

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            categoryController.lookupCategories(name, null);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).lookupCategories(name);
    }

    @Test
    void lookupCategories_withLimit_shouldReturnSuccessResponse() {
        // Given
        String name = "rock";
        Integer limit = 5;
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(name, limit)).thenReturn(expectedCategories);
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.success(expectedCategories);

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            categoryController.lookupCategories(name, limit);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).lookupCategories(name, limit);
    }

    @Test
    void lookupCategories_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        String name = "rock";
        String errorMessage = "Test error";
        
        when(categoryService.lookupCategories(name)).thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> expectedResponse = 
            ResponseWrapper.failure(String.format("Failed to lookup categories: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> actualResponse = 
            categoryController.lookupCategories(name, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).lookupCategories(name);
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
    void saveCategory_whenSelfParentValidationFails_shouldReturnFailureResponse() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(1L)
            .name("Test Category")
            .parentId(1L) // Same as ID - self-parent
            .build();
        String errorMessage = "Category cannot be parent of itself";
        
        when(categoryService.saveCategory(request)).thenThrow(new IllegalArgumentException(errorMessage));
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

    @Test
    void findBoundCategories_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            1L, dataSource, 101L, "Rock"
        );
        List<BoundEntityProjection> expectedBindings = List.of(projection);
        
        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenReturn(expectedBindings);
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse =
            ResponseWrapper.success(expectedBindings);

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse =
            categoryController.findBoundCategories(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).findBoundCategories(dataSource, externalIds);
    }

    @Test
    void findBoundCategories_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        String errorMessage = "Test error";
        
        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to get bound categories: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<BoundEntityProjection>>> actualResponse =
            categoryController.findBoundCategories(dataSource, externalIds);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).findBoundCategories(dataSource, externalIds);
    }

    @Test
    void bindToExisting_shouldReturnSuccessResponse() {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = 
            ResponseWrapper.success(projection);
        
        when(categoryService.bindToExisting(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            categoryController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).bindToExisting(dataSource, externalId, request);
    }

    @Test
    void bindToExisting_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long categoryId = 101L;
        String errorMessage = "Test error";
        
        CategoryBindToExistingRequestDTO request = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to bind category to existing: %s", errorMessage));
        
        when(categoryService.bindToExisting(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            categoryController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).bindToExisting(dataSource, externalId, request);
    }

    @Test
    void createAndBind_shouldReturnSuccessResponse() {
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
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse = 
            ResponseWrapper.success(projection);
        
        when(categoryService.createAndBind(dataSource, externalId, request))
            .thenReturn(projection);

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            categoryController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).createAndBind(dataSource, externalId, request);
    }

    @Test
    void createAndBind_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        String errorMessage = "Test error";
        
        CategoryCreateAndBindRequestDTO request = CategoryCreateAndBindRequestDTO.builder()
            .name(categoryName)
            .build();
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to create and bind category: %s", errorMessage));
        
        when(categoryService.createAndBind(dataSource, externalId, request))
            .thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<BoundEntityProjection>> actualResponse =
            categoryController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).createAndBind(dataSource, externalId, request);
    }

    @Test
    void unbindCategory_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(categoryService.unbindCategory(dataSource, externalId)).thenReturn(true);
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse = ResponseWrapper.success(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            categoryController.unbindCategory(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).unbindCategory(dataSource, externalId);
    }

    @Test
    void unbindCategory_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String errorMessage = "Test error";

        when(categoryService.unbindCategory(dataSource, externalId))
            .thenThrow(new RuntimeException(errorMessage));
        ResponseEntity<ResponseWrapper<Boolean>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to unbind category: %s", errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> actualResponse =
            categoryController.unbindCategory(dataSource, externalId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse, actualResponse);
        verify(categoryService).unbindCategory(dataSource, externalId);
    }
    
    @Test
    void batchLookupCategories_shouldReturnSuccessResponse() {
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
        ResponseEntity<ResponseWrapper<CategoryBatchLookupResponseDTO>> actualResponse =
            categoryController.batchLookupCategories(request);
        
        // Then
        assertEquals(HttpStatus.OK, actualResponse.getStatusCode());
        assertEquals(ResponseWrapper.successBody(expectedResponse), actualResponse.getBody());
        
        verify(categoryService).batchLookupCategories(request);
    }
    
    @Test
    void batchLookupCategories_whenExceptionThrown_shouldReturnFailureResponse() {
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
        
        ResponseEntity<ResponseWrapper<CategoryBatchLookupResponseDTO>> expectedResponse =
            ResponseWrapper.failure(String.format("Failed to batch lookup categories: %s", errorMessage));
        
        // When
        ResponseEntity<ResponseWrapper<CategoryBatchLookupResponseDTO>> actualResponse =
            categoryController.batchLookupCategories(request);
        
        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, actualResponse.getStatusCode());
        assertEquals(expectedResponse.getBody().isSuccess(), actualResponse.getBody().isSuccess());
        assertEquals(expectedResponse.getBody().getMessage(), actualResponse.getBody().getMessage());
        
        verify(categoryService).batchLookupCategories(request);
    }
}
