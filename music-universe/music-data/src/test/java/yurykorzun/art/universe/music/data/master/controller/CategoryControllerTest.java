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
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.TestCategoryHierarchyProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.BindingService;
import yurykorzun.art.universe.music.data.master.service.CategoryService;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private BindingService bindingService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void findCategories_shouldReturnPageOfCategories() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        
        CategoryHierarchyProjection category1 = new TestCategoryHierarchyProjectionImpl(
            1L, "Genre", 1L, 1L, null, 1, "Dimension 1", "Dimension 1", null);
        CategoryHierarchyProjection category2 = new TestCategoryHierarchyProjectionImpl(
            2L, "Subgenre", 1L, 1L, 1L, 2, "Dimension 1", "Dimension 1", "Genre");
        
        List<CategoryHierarchyProjection> categories = Arrays.asList(category1, category2);
        Page<CategoryHierarchyProjection> expectedPage = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryService.findCategories(search, pageable)).thenReturn(expectedPage);

        // When
        Page<CategoryHierarchyProjection> result = categoryController.findCategories(search, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(categoryService).findCategories(search, pageable);
    }

    @Test
    void findCategories_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String search = "genre";
        Pageable pageable = PageRequest.of(0, 10);
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.findCategories(search, pageable)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.findCategories(search, pageable)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).findCategories(search, pageable);
    }

    @Test
    void lookupCategories_shouldReturnListOfLookupResults() {
        // Given
        String search = "rock";
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenReturn(expectedCategories);

        // When
        List<LookupResultDTO> result = categoryController.lookupCategories(search, null);

        // Then
        assertEquals(expectedCategories, result);
        verify(categoryService).lookupCategories(any(LookupRequestDTO.class));
    }

    @Test
    void lookupCategories_withLimit_shouldReturnListOfLookupResults() {
        // Given
        String search = "rock";
        Integer limit = 5;
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = Arrays.asList(category1, category2);
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenReturn(expectedCategories);

        // When
        List<LookupResultDTO> result = categoryController.lookupCategories(search, limit);

        // Then
        assertEquals(expectedCategories, result);
        verify(categoryService).lookupCategories(any(LookupRequestDTO.class));
    }

    @Test
    void lookupCategories_whenExceptionThrown_shouldPassThroughException() {
        // Given
        String search = "rock";
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.lookupCategories(search, null)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).lookupCategories(any(LookupRequestDTO.class));
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
    void saveCategory_whenExceptionThrown_shouldPassThroughException() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .dimensionId(1L)
            .build();
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.saveCategory(request)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.saveCategory(request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).saveCategory(request);
    }

    @Test
    void saveCategory_whenSelfParentValidationFails_shouldPassThroughException() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .id(1L)
            .name("Test Category")
            .parentId(1L) // Same as ID - self-parent
            .build();
        IllegalArgumentException expectedException = new IllegalArgumentException("Category cannot be parent of itself");
        
        when(categoryService.saveCategory(request)).thenThrow(expectedException);

        // When & Then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> 
            categoryController.saveCategory(request)
        );
        
        assertSame(expectedException, exception);
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
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            categoryController.deleteCategory(id)
        );
        
        assertEquals("Category not found with id: " + id, exception.getMessage());
        verify(categoryService).deleteCategory(id);
    }

    @Test
    void deleteCategory_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long id = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.deleteCategory(id)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.deleteCategory(id)
        );
        
        assertSame(expectedException, exception);
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
    void findBoundCategories_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(1L, 2L);
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.findBoundCategories(dataSource, externalIds)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).findBoundCategories(dataSource, externalIds);
    }

    @Test
    void bindToExisting_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, masterId, "Rock"
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
    void bindToExisting_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        Long masterId = 101L;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        EntityBindToExistingRequestDTO request = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        when(categoryService.bindToExisting(dataSource, externalId, request))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.bindToExisting(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).bindToExisting(dataSource, externalId, request);
    }

    @Test
    void createAndBind_shouldReturnBoundEntityProjection() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(categoryName)
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
    void createAndBind_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        String categoryName = "New Genre";
        RuntimeException expectedException = new RuntimeException("Test error");
        
        EntityCreateAndBindRequestDTO request = EntityCreateAndBindRequestDTO.builder()
            .entityName(categoryName)
            .build();
        
        when(categoryService.createAndBind(dataSource, externalId, request))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.createAndBind(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
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
    void unbindCategory_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(categoryService.unbindCategory(dataSource, externalId))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.unbindCategory(dataSource, externalId)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).unbindCategory(dataSource, externalId);
    }
    
    @Test
    void batchLookupCategories_shouldReturnBatchLookupResponseDTO() {
        // Given
        List<String> names = List.of("rock", "jazz");
        Integer limit = 10;
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(names))
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
        
        BatchLookupResponseDTO expectedResponse = BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
        
        when(categoryService.batchLookupCategories(request)).thenReturn(expectedResponse);
        
        // When
        BatchLookupResponseDTO result = categoryController.batchLookupCategories(request);
        
        // Then
        assertEquals(expectedResponse, result);
        verify(categoryService).batchLookupCategories(request);
    }
    
    @Test
    void batchLookupCategories_whenExceptionThrown_shouldPassThroughException() {
        // Given
        List<String> names = List.of("rock", "jazz");
        Integer limit = 10;
        RuntimeException expectedException = new RuntimeException("Test error");
        
        BaseBatchLookupRequestDTO request = BaseBatchLookupRequestDTO.builder()
            .searchRequests(createLookupRequests(names))
            .limit(limit)
            .build();
        
        when(categoryService.batchLookupCategories(request))
            .thenThrow(expectedException);
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.batchLookupCategories(request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).batchLookupCategories(request);
    }
    
    /**
     * Helper method to convert a list of search terms to a list of LookupRequestDTO
     */
    private List<LookupRequestDTO> createLookupRequests(List<String> searchTerms) {
        return searchTerms.stream()
            .map(term -> LookupRequestDTO.builder().search(term).build())
            .collect(Collectors.toList());
    }
}
