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
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.common.exception.DataUpdateException;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
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
        
        CategoryDto category1 = CategoryDto.builder()
            .id(1L)
            .name("Genre")
            .build();
        CategoryDto category2 = CategoryDto.builder()
            .id(2L)
            .name("Subgenre")
            .build();
        
        List<CategoryDto> categories = Arrays.asList(category1, category2);
        Page<CategoryDto> expectedPage = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryService.findCategories(search, pageable)).thenReturn(expectedPage);

        // When
        Page<CategoryDto> result = categoryController.findCategories(search, pageable);

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
    void saveCategory_shouldReturnCategoryDto() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .build();
        
        CategoryDto savedCategory = CategoryDto.builder()
            .id(1L)
            .name("Genre")
            .build();
        
        when(categoryService.saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(savedCategory);

        // When
        CategoryDto result = categoryController.saveCategory(request);

        // Then
        assertEquals(savedCategory, result);
        verify(categoryService).saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void saveCategory_whenExceptionThrown_shouldPassThroughException() {
        // Given
        CategorySaveRequestDTO request = CategorySaveRequestDTO.builder()
            .name("Genre")
            .build();
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.saveCategory(request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).saveCategory(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void findCategoriesWithParents_shouldReturnListOfCategoriesWithParents() {
        // Given
        String search = "rock";
        Pageable pageable = PageRequest.of(0, 10);
        CategoryDto parent1 = CategoryDto.builder().id(1L).name("Music").build();
        CategoryDto parent2 = CategoryDto.builder().id(2L).name("Genre").build();
        
        CategoryWithParentsDto category = CategoryWithParentsDto.builder()
            .id(3L)
            .name("Rock")
            .parents(Arrays.asList(parent1, parent2))
            .build();
        
        List<CategoryWithParentsDto> categories = Arrays.asList(category);
        Page<CategoryWithParentsDto> expectedPage = new PageImpl<>(categories, pageable, categories.size());
        
        when(categoryService.findCategoriesWithParents(search, pageable)).thenReturn(expectedPage);

        // When
        Page<CategoryWithParentsDto> result = categoryController.findCategoriesWithParents(search, pageable);

        // Then
        assertEquals(expectedPage, result);
        verify(categoryService).findCategoriesWithParents(search, pageable);
    }

    @Test
    void saveCategoryWithParents_shouldReturnCategoryWithParentsDto() {
        // Given
        CategorySaveWithParentsRequestDTO request = CategorySaveWithParentsRequestDTO.builder()
            .name("Rock")
            .parents(Arrays.asList(1L, 2L))
            .build();
        
        CategoryDto parent1 = CategoryDto.builder().id(1L).name("Music").build();
        CategoryDto parent2 = CategoryDto.builder().id(2L).name("Genre").build();
        
        CategoryWithParentsDto savedCategory = CategoryWithParentsDto.builder()
            .id(3L)
            .name("Rock")
            .parents(Arrays.asList(parent1, parent2))
            .build();
        
        when(categoryService.saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenReturn(savedCategory);

        // When
        CategoryWithParentsDto result = categoryController.saveCategoryWithParents(request);

        // Then
        assertEquals(savedCategory, result);
        verify(categoryService).saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    @Test
    void saveCategoryWithParents_whenExceptionThrown_shouldPassThroughException() {
        // Given
        CategorySaveWithParentsRequestDTO request = CategorySaveWithParentsRequestDTO.builder()
            .name("Rock")
            .parents(Arrays.asList(1L, 2L))
            .build();
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.saveCategoryWithParents(request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
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
        
        when(categoryService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryController.bindToExisting(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(categoryService).bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
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
        
        when(categoryService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.bindToExisting(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
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
        
        when(categoryService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryController.createAndBind(dataSource, externalId, request);

        // Then
        assertEquals(projection, result);
        verify(categoryService).createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
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
        
        when(categoryService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED))
            .thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.createAndBind(dataSource, externalId, request)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
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
    
    @Test
    void getCategoryDag_shouldReturnCategoryDagDTO() {
        // Given
        CategoryDagNodeDTO node1 = CategoryDagNodeDTO.builder()
            .id(1L)
            .name("Rock")
            .isRoot(true)
            .build();
        CategoryDagNodeDTO node2 = CategoryDagNodeDTO.builder()
            .id(2L)
            .name("Alternative Rock")
            .isRoot(false)
            .build();
        
        CategoryDagEdgeDTO edge = CategoryDagEdgeDTO.builder()
            .source(1L)
            .target(2L)
            .build();
        
        CategoryDagDTO expectedDag = CategoryDagDTO.builder()
            .nodes(List.of(node1, node2))
            .edges(List.of(edge))
            .build();
        
        when(categoryService.getCategoryDag()).thenReturn(expectedDag);

        // When
        CategoryDagDTO result = categoryController.getCategoryDag();

        // Then
        assertEquals(expectedDag, result);
        verify(categoryService).getCategoryDag();
    }

    @Test
    void getCategoryDag_whenExceptionThrown_shouldPassThroughException() {
        // Given
        RuntimeException expectedException = new RuntimeException("Test error");
        
        when(categoryService.getCategoryDag()).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            categoryController.getCategoryDag()
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).getCategoryDag();
    }
    
    @Test
    void createCategoryRelation_shouldCallService() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        // When
        categoryController.createCategoryRelation(relation);
        
        // Then
        verify(categoryService).createCategoryRelation(relation);
    }
    
    @Test
    void createCategoryRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        DataUpdateException expectedException = new DataUpdateException("Test error");
        
        doThrow(expectedException).when(categoryService).createCategoryRelation(relation);
        
        // When & Then
        DataUpdateException exception = assertThrows(DataUpdateException.class, () -> 
            categoryController.createCategoryRelation(relation)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).createCategoryRelation(relation);
    }
    
    @Test
    void deleteCategoryRelation_shouldCallService() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        // When
        categoryController.deleteCategoryRelation(relation);
        
        // Then
        verify(categoryService).deleteCategoryRelation(relation);
    }
    
    @Test
    void deleteCategoryRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        CustomEntityNotFoundException expectedException = new CustomEntityNotFoundException("Test error");
        
        doThrow(expectedException).when(categoryService).deleteCategoryRelation(relation);
        
        // When & Then
        CustomEntityNotFoundException exception = assertThrows(CustomEntityNotFoundException.class, () ->
            categoryController.deleteCategoryRelation(relation)
        );
        
        assertSame(expectedException, exception);
        verify(categoryService).deleteCategoryRelation(relation);
    }

    @Test
    void getCategoryWithParents_shouldReturnCategoryWithParents() {
        // Given
        Long id = 1L;
        CategoryDto parent1 = CategoryDto.builder().id(2L).name("Music").build();
        CategoryDto parent2 = CategoryDto.builder().id(3L).name("Genre").build();

        CategoryWithParentsDto expectedCategory = CategoryWithParentsDto.builder()
                .id(id)
                .name("Rock")
                .parents(Arrays.asList(parent1, parent2))
                .build();

        when(categoryService.getCategoryWithParents(id)).thenReturn(expectedCategory);

        // When
        CategoryWithParentsDto result = categoryController.getCategoryWithParents(id);

        // Then
        assertEquals(expectedCategory, result);
        verify(categoryService).getCategoryWithParents(id);
    }

    @Test
    void getCategoryWithParents_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long id = 1L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(categoryService.getCategoryWithParents(id)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                categoryController.getCategoryWithParents(id)
        );

        assertSame(expectedException, exception);
        verify(categoryService).getCategoryWithParents(id);
    }
}