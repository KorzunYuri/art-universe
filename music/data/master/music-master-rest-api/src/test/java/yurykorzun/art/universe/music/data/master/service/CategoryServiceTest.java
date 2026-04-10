package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.*;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryCategoryRepository;
import yurykorzun.art.universe.music.data.master.exception.DiamondRelationException;
import yurykorzun.art.universe.music.data.master.exception.CycleRelationException;
import yurykorzun.art.universe.common.exception.DataUpdateException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
    private CategoryCategoryRepository categoryCategoryRepository;

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
    void findCategory_shouldReturnSingleBoundBoundCategory() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        TestBoundEntityProjectionImpl projection = new TestBoundEntityProjectionImpl(
            externalId, dataSource, 101L, "Rock"
        );
        
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(projection);

        // When
        BoundEntityProjection result = categoryService.findBoundCategory(dataSource, externalId);

        // Then
        assertEquals(projection, result);
        verify(categoryBindingRepository).findBoundCategoryForDataSource(dataSource, externalId);
    }

    @Test
    void findBoundCategory_whenNotFound_shouldReturnNull() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 1L;
        
        when(categoryBindingRepository.findBoundCategoryForDataSource(dataSource, externalId))
            .thenReturn(null);

        // When
        BoundEntityProjection result = categoryService.findBoundCategory(dataSource, externalId);

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
        BoundEntityProjection result = categoryService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

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
            categoryService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        
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
        BoundEntityProjection result = categoryService.bindToExisting(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

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
        BoundEntityProjection result = categoryService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

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
            categoryService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        
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
        BoundEntityProjection result = categoryService.createAndBind(dataSource, externalId, request, Origin.MANUAL, MasterApprovalStatus.APPROVED);

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
    void saveCategoryWithParents_shouldCreateCategoryAndRelations() {
        // Given
        CategorySaveWithParentsRequestDTO request = CategorySaveWithParentsRequestDTO.builder()
            .name("Rock")
            .parents(Arrays.asList(1L, 2L))
            .build();
        
        Category savedCategory = Category.builder()
            .id(3L)
            .name("Rock")
            .build();
        
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(savedCategory));
        
        // When
        CategoryWithParentsDto result = categoryService.saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED);
        
        // Then
        assertEquals(3L, result.getId());
        assertEquals("Rock", result.getName());
        
        verify(categoryRepository).save(any(Category.class));
        verify(categoryCategoryRepository, times(2)).save(any(CategoryCategory.class));
    }

    @Test
    void getCategoryDag_shouldReturnCategoryDagWithNodesAndEdges() {
        // Given
        Category rootCategory = Category.builder()
            .id(1L)
            .name("Rock")
            .build();
        
        Category childCategory = Category.builder()
            .id(2L)
            .name("Alternative Rock")
            .build();
        
        CategoryCategory relation = CategoryCategory.builder()
            .sourceCategoryId(1L)
            .targetCategoryId(2L)
            .build();
        
        List<Category> categories = List.of(rootCategory, childCategory);
        List<CategoryCategory> relations = List.of(relation);
        
        List<CategoryCountsProjection> counts = List.of(
            new TestCategoryCountsProjectionImpl(1L, "Rock", 1, 0, 0),
            new TestCategoryCountsProjectionImpl(2L, "Alternative Rock", 0, 0, 0)
        );
        
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryCategoryRepository.findAll()).thenReturn(relations);
        when(categoryRepository.findCategoriesWithCounts()).thenReturn(counts);

        // When
        CategoryDagDTO result = categoryService.getCategoryDag();

        // Then
        assertEquals(2, result.getNodes().size());
        assertEquals(1, result.getEdges().size());
        
        // Verify root node
        CategoryDagNodeDTO rootNode = result.getNodes().stream()
            .filter(node -> node.getId().equals(1L))
            .findFirst()
            .orElseThrow();
        assertEquals("Rock", rootNode.getName());
        assertTrue(rootNode.isRoot());
        assertEquals(1, rootNode.getChildrenCount());
        assertEquals(0, rootNode.getArtistsCount());
        assertEquals(0, rootNode.getTracksCount());
        
        // Verify child node
        CategoryDagNodeDTO childNode = result.getNodes().stream()
            .filter(node -> node.getId().equals(2L))
            .findFirst()
            .orElseThrow();
        assertEquals("Alternative Rock", childNode.getName());
        assertFalse(childNode.isRoot());
        assertEquals(0, childNode.getChildrenCount());
        assertEquals(0, childNode.getArtistsCount());
        assertEquals(0, childNode.getTracksCount());
        
        // Verify edge
        CategoryDagEdgeDTO edge = result.getEdges().getFirst();
        assertEquals(1L, edge.getSource());
        assertEquals(2L, edge.getTarget());
        
        verify(categoryRepository).findAll();
        verify(categoryCategoryRepository).findAll();
    }

    @Test
    void getCategoryDag_withOnlyRootCategories_shouldReturnNodesWithoutEdges() {
        // Given
        Category category1 = Category.builder()
            .id(1L)
            .name("Rock")
            .build();
        
        Category category2 = Category.builder()
            .id(2L)
            .name("Jazz")
            .build();
        
        List<Category> categories = List.of(category1, category2);
        List<CategoryCategory> emptyRelations = List.of();
        
        when(categoryRepository.findAll()).thenReturn(categories);
        when(categoryCategoryRepository.findAll()).thenReturn(emptyRelations);
        when(categoryRepository.findCategoriesWithCounts()).thenReturn(List.of());

        // When
        CategoryDagDTO result = categoryService.getCategoryDag();

        // Then
        assertEquals(2, result.getNodes().size());
        assertEquals(0, result.getEdges().size());
        
        result.getNodes().forEach(node -> {
            assertTrue(node.isRoot());
            assertEquals(0, node.getChildrenCount());
            assertEquals(0, node.getArtistsCount());
            assertEquals(0, node.getTracksCount());
        });
        
        verify(categoryRepository).findAll();
        verify(categoryCategoryRepository).findAll();
    }
    
    @Test
    @Disabled("Diamond checking temporarily disabled")
    void saveCategoryWithParents_whenWouldCreateDiamond_shouldThrowException() {
        // Given: A -> B -> C hierarchy exists
        Category categoryC = Category.builder().id(3L).name("C").build();
        
        when(categoryRepository.findById(3L)).thenReturn(Optional.of(categoryC));
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryC);

        // Mock existing relations: A -> B -> C
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(2L));
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(3L));
        when(categoryCategoryRepository.findChildIds(3L)).thenReturn(List.of()); // Add missing mock
        
        CategorySaveWithParentsRequestDTO request = CategorySaveWithParentsRequestDTO.builder()
            .id(3L) // Update existing category
            .name("C")
            .parents(List.of(1L)) // Try to create A -> C (diamond: A -> B -> C and A -> C)
            .build();
        
        // When & Then
        DiamondRelationException exception = assertThrows(DiamondRelationException.class, () ->
            categoryService.saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        
        assertTrue(exception.getMessage().contains("would create a diamond"));
    }
    
    @Test
    void saveCategoryWithParents_whenWouldCreateCycle_shouldThrowException() {
        // Given: A -> B hierarchy exists
        Category categoryA = Category.builder().id(1L).name("A").build();
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryA));
        when(categoryRepository.save(any(Category.class))).thenReturn(categoryA);
        
        // Mock existing relations: B -> A (so A -> B would create cycle)
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(2L)); // A -> B path
        
        CategorySaveWithParentsRequestDTO request = CategorySaveWithParentsRequestDTO.builder()
            .id(1L) // Update existing category A
            .name("A")
            .parents(List.of(2L)) // Try to create B -> A (cycle: A -> B -> A)
            .build();
        
        // When & Then
        CycleRelationException exception = assertThrows(CycleRelationException.class, () ->
            categoryService.saveCategoryWithParents(request, Origin.MANUAL, MasterApprovalStatus.APPROVED));
        
        assertTrue(exception.getMessage().contains("would create a cycle"));
    }
    
    @Test
    @Disabled("Diamond checking temporarily disabled")
    void deleteCategory_whenWouldCreateDiamond_shouldThrowException() {
        // Given: A -> B -> C and A -> D -> C (diamond through B)
        when(categoryRepository.existsById(2L)).thenReturn(true);
        
        // B has parents: A and children: C
        when(categoryCategoryRepository.findParentIds(2L)).thenReturn(List.of(1L));
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(3L));
        
        // Mock that A -> C doesn't exist yet
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 3L))
            .thenReturn(false);
        
        // Mock that path A -> C already exists through D
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(4L)); // A -> D
        when(categoryCategoryRepository.findChildIds(4L)).thenReturn(List.of(3L)); // D -> C
        when(categoryCategoryRepository.findChildIds(3L)).thenReturn(List.of()); // Add missing mock
        
        // When & Then
        DiamondRelationException exception = assertThrows(DiamondRelationException.class, () ->
            categoryService.deleteCategory(2L));
        
        assertTrue(exception.getMessage().contains("would create a diamond"));
    }
    
    @Test
    void deleteCategory_whenWouldCreateCycle_shouldThrowException() {
        // Given: A -> B -> C -> A cycle would be created by deleting B
        when(categoryRepository.existsById(2L)).thenReturn(true);
        
        // B has parent A and child C
        when(categoryCategoryRepository.findParentIds(2L)).thenReturn(List.of(1L));
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(3L));
        
        // Mock that A -> C doesn't exist yet
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 3L))
            .thenReturn(false);
        
        // Mock that path C -> A exists (would create cycle A -> C -> A)
        when(categoryCategoryRepository.findChildIds(3L)).thenReturn(List.of(1L)); // C -> A
        
        // When & Then
        CycleRelationException exception = assertThrows(CycleRelationException.class, () ->
            categoryService.deleteCategory(2L));
        
        assertTrue(exception.getMessage().contains("would create a cycle"));
    }

    @Test
    void createCategoryRelation_shouldCreateRelation() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 2L))
            .thenReturn(false);
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of());
        
        // When
        categoryService.createCategoryRelation(relation);
        
        // Then
        verify(categoryCategoryRepository).save(any(CategoryCategory.class));
    }
    
    @Test
    void createCategoryRelation_whenRelationExists_shouldThrowException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 2L))
            .thenReturn(true);
        
        // When & Then
        DataUpdateException exception = assertThrows(DataUpdateException.class, () ->
            categoryService.createCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("Relation already exists"));
        verify(categoryCategoryRepository, never()).save(any());
    }
    
    @Test
    void createCategoryRelation_whenWouldCreateCycle_shouldThrowException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 2L))
            .thenReturn(false);
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(1L)); // B -> A path
        
        // When & Then
        CycleRelationException exception = assertThrows(CycleRelationException.class, () ->
            categoryService.createCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("would create a cycle"));
        verify(categoryCategoryRepository, never()).save(any());
    }
    
    @Test
    @Disabled("Diamond checking temporarily disabled")
    void createCategoryRelation_whenWouldCreateDiamond_shouldThrowException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(3L)
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(1L, 3L))
            .thenReturn(false);
        when(categoryCategoryRepository.findChildIds(3L)).thenReturn(List.of());
        when(categoryCategoryRepository.findChildIds(1L)).thenReturn(List.of(2L)); // A -> B
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(3L)); // B -> C
        
        // When & Then
        DiamondRelationException exception = assertThrows(DiamondRelationException.class, () ->
            categoryService.createCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("would create a diamond"));
        verify(categoryCategoryRepository, never()).save(any());
    }
    
    @Test
    @Disabled("Diamond checking temporarily disabled")
    void createCategoryRelation_whenWouldCreateDiamondThroughCommonDescendant_shouldThrowException() {
        // Given: A->B->D->E and A->C->M->E (two paths from A to E)
        // When trying to create B->C, it would create diamond through common descendant E
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(2L) // B
            .targetId(3L) // C
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(2L, 3L)).thenReturn(false);
        
        // Mock path checking: no direct path from B to C
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of(4L)); // B->D
        when(categoryCategoryRepository.findChildIds(4L)).thenReturn(List.of(5L)); // D->E
        when(categoryCategoryRepository.findChildIds(5L)).thenReturn(List.of()); // E has no children
        when(categoryCategoryRepository.findChildIds(3L)).thenReturn(List.of(6L)); // C->M
        when(categoryCategoryRepository.findChildIds(6L)).thenReturn(List.of(5L)); // M->E
        
        // When & Then
        DiamondRelationException exception = assertThrows(DiamondRelationException.class, () ->
            categoryService.createCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("would create a diamond"));
        verify(categoryCategoryRepository, never()).save(any());
    }

    @Test
    @Disabled("Diamond checking temporarily disabled")
    void createCategoryRelation_whenWouldCreateDiamondThroughCommonAncestor_shouldThrowException() {
        // Given: A->B and A->C->D (common ancestor A)
        // When trying to create B->D, it would create diamond through common ancestor A
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(2L) // B
            .targetId(4L) // D
            .build();
        
        when(categoryCategoryRepository.existsBySourceCategoryIdAndTargetCategoryId(2L, 4L)).thenReturn(false);
        
        // Mock path checking: no direct path from B to D
        when(categoryCategoryRepository.findChildIds(2L)).thenReturn(List.of()); // B has no children
        when(categoryCategoryRepository.findChildIds(4L)).thenReturn(List.of()); // D has no children
        
        // Mock ancestor checking: B and C both have A as ancestor
        when(categoryCategoryRepository.findParentIds(2L)).thenReturn(List.of(1L)); // B->A
        when(categoryCategoryRepository.findParentIds(3L)).thenReturn(List.of(1L)); // C->A
        when(categoryCategoryRepository.findParentIds(4L)).thenReturn(List.of(3L)); // D->C
        when(categoryCategoryRepository.findParentIds(1L)).thenReturn(List.of()); // A has no parents
        
        // When & Then
        DiamondRelationException exception = assertThrows(DiamondRelationException.class, () ->
            categoryService.createCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("would create a diamond"));
        verify(categoryCategoryRepository, never()).save(any());
    }

    @Test
    void deleteCategoryRelation_shouldDeleteRelation() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        CategoryCategory existingRelation = CategoryCategory.builder()
            .id(1L)
            .sourceCategoryId(1L)
            .targetCategoryId(2L)
            .build();
        
        when(categoryCategoryRepository.findAll()).thenReturn(List.of(existingRelation));
        
        // When
        categoryService.deleteCategoryRelation(relation);
        
        // Then
        verify(categoryCategoryRepository).delete(existingRelation);
    }
    
    @Test
    void deleteCategoryRelation_whenRelationNotFound_shouldThrowException() {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        when(categoryCategoryRepository.findAll()).thenReturn(List.of());
        
        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
            categoryService.deleteCategoryRelation(relation));
        
        assertTrue(exception.getMessage().contains("Relation not found"));
        verify(categoryCategoryRepository, never()).delete(any());
    }


    @Test
    void getCategoryWithParents_shouldReturnCategoryWithParents() {
        // Given
        Long id = 1L;
        Category category = Category.builder()
                .id(id)
                .name("Rock")
                .build();

        when(categoryRepository.findById(id)).thenReturn(Optional.of(category));

        // When
        CategoryWithParentsDto result = categoryService.getCategoryWithParents(id);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Rock", result.getName());
        verify(categoryRepository).findById(id);
    }

    @Test
    void getCategoryWithParents_whenNotFound_shouldThrowException() {
        // Given
        Long id = 1L;

        when(categoryRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                categoryService.getCategoryWithParents(id));

        assertEquals("Category not found with id " + id, exception.getMessage());
        verify(categoryRepository).findById(id);
    }
}