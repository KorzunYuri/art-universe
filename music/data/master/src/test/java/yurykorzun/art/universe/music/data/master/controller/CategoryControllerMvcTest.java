package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.dto.CategoryDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryWithParentsDto;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagNodeDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryDagEdgeDTO;
import yurykorzun.art.universe.music.data.master.dto.CategoryRelationDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.BindingService;
import yurykorzun.art.universe.music.data.master.service.CategoryService;
import yurykorzun.art.universe.music.data.master.common.archetypes.BaseMasterDataMvcTest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

    @MockitoBean
    private BindingService bindingService;

    @Autowired
    private MockMvc mockMvc;

    private List<TestBoundEntityProjectionImpl> mockCategoryBindings;

    @BeforeEach
    void setup() {
        TestBoundEntityProjectionImpl binding1 = new TestBoundEntityProjectionImpl(123L, DataSource.LASTFM, 321L, "Rock");
        TestBoundEntityProjectionImpl binding2 = new TestBoundEntityProjectionImpl(456L, DataSource.LASTFM, 654L, "Jazz");
        TestBoundEntityProjectionImpl binding3 = new TestBoundEntityProjectionImpl(789L, DataSource.SPOTIFY, 987L, "Electronic");
        mockCategoryBindings = List.of(binding1, binding2, binding3);
    }

    @Test
    void whenFindBoundCategories_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        final DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
        when(categoryService.findBoundCategories(eq(dataSource), any()))
            .thenReturn(emptyList);

        // When/Then
        mockMvc.perform(get("/api/v1/categories/bound/{dataSource}", dataSource)
                .param("externalIds", "999,888"))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundCategories_withMatchingCategories_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<BoundEntityProjection> expectedCategories = mockCategoryBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedCategories.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedCategories);

        when(categoryService.findBoundCategories(dataSource, externalIds)).thenReturn(expectedCategories);

        // When & Then
        mockMvc.perform(get("/api/v1/categories/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindBoundCategories_withSingleMatchingCategory_shouldReturnMatchingOnly() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<BoundEntityProjection> expectedCategories = mockCategoryBindings.stream()
            .filter(p -> dataSource.equals(p.getDataSource()))
            .map(BoundEntityProjection.class::cast)
            .toList();
        List<Long> externalIds = expectedCategories.stream().map(BoundEntityProjection::getExternalId).toList();
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String expectedJson = objectMapper.writeValueAsString(expectedCategories);

        when(categoryService.findBoundCategories(dataSource, externalIds)).thenReturn(expectedCategories);

        // When & Then
        mockMvc.perform(get("/api/v1/categories/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void shouldReturnError_whenServiceFails() throws Exception {
        // Given
        DataSource dataSource = DataSource.SPOTIFY;
        List<Long> externalIds = Arrays.asList(123L, 456L);
        final String[] externalIdParams = externalIds.stream()
            .map(String::valueOf)
            .toArray(String[]::new);
        String errorMessage = "Service error occurred";

        when(categoryService.findBoundCategories(dataSource, externalIds))
            .thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/categories/bound/{dataSource}", dataSource)
                .param("externalIds", externalIdParams))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenLookupCategories_shouldReturnMatchingCategories() throws Exception {
        // Given
        String searchQuery = "rock";
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = List.of(category1, category2);
        
        String expectedJson = objectMapper.writeValueAsString(expectedCategories);
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenReturn(expectedCategories);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("search", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupCategories_withLimit_shouldReturnLimitedResults() throws Exception {
        // Given
        String searchQuery = "rock";
        Integer limit = 5;
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = List.of(category1, category2);
        
        String expectedJson = objectMapper.writeValueAsString(expectedCategories);
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenReturn(expectedCategories);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("search", searchQuery)
                .param("limit", limit.toString()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupCategories_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        String searchQuery = "nonexistent";
        List<LookupResultDTO> emptyList = Collections.emptyList();
        String expectedJson = objectMapper.writeValueAsString(emptyList);
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class))).thenReturn(emptyList);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("search", searchQuery))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupCategories_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "rock";
        String errorMessage = "Search error occurred";
        
        when(categoryService.lookupCategories(any(LookupRequestDTO.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("search", searchQuery))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenGetCategoryDag_shouldReturnCategoryDag() throws Exception {
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
        
        String expectedJson = objectMapper.writeValueAsString(expectedDag);
        
        when(categoryService.getCategoryDag()).thenReturn(expectedDag);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/dag"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenGetCategoryDag_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String errorMessage = "DAG error occurred";
        
        when(categoryService.getCategoryDag())
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/dag"))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenFindCategoriesWithParents_shouldReturnCategoriesWithParents() throws Exception {
        // Given
        String searchQuery = "rock";
        CategoryDto parent1 = CategoryDto.builder().id(1L).name("Music").build();
        CategoryDto parent2 = CategoryDto.builder().id(2L).name("Genre").build();
        
        CategoryWithParentsDto category = CategoryWithParentsDto.builder()
            .id(3L)
            .name("Rock")
            .parents(Arrays.asList(parent1, parent2))
            .build();
        
        List<CategoryWithParentsDto> categories = Arrays.asList(category);
        Page<CategoryWithParentsDto> expectedPage = new PageImpl<>(categories, PageRequest.of(0, 20), categories.size());
        String expectedJson = objectMapper.writeValueAsString(expectedPage);
        
        when(categoryService.findCategoriesWithParents(eq(searchQuery), any(Pageable.class))).thenReturn(expectedPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/with-parents")
                .param("search", searchQuery))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindCategoriesWithParents_withNoResults_shouldReturnEmptyList() throws Exception {
        // Given
        String searchQuery = "nonexistent";
        List<CategoryWithParentsDto> emptyList = Collections.emptyList();
        Page<CategoryWithParentsDto> emptyPage = new PageImpl<>(emptyList, PageRequest.of(0, 20), 0);
        String expectedJson = objectMapper.writeValueAsString(emptyPage);
        
        when(categoryService.findCategoriesWithParents(eq(searchQuery), any(Pageable.class))).thenReturn(emptyPage);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/with-parents")
                .param("search", searchQuery))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenFindCategoriesWithParents_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "rock";
        String errorMessage = "Search error occurred";
        
        when(categoryService.findCategoriesWithParents(eq(searchQuery), any(Pageable.class)))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/with-parents")
                .param("search", searchQuery))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenCreateCategoryRelation_shouldReturnOk() throws Exception {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        String requestJson = objectMapper.writeValueAsString(relation);
        
        // When & Then
        mockMvc.perform(post("/api/v1/categories/relations")
                .contentType("application/json")
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isOk());
    }
    
    @Test
    void whenCreateCategoryRelation_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        String requestJson = objectMapper.writeValueAsString(relation);
        String errorMessage = "Relation error occurred";
        
        doThrow(new RuntimeException(errorMessage))
            .when(categoryService).createCategoryRelation(any(CategoryRelationDTO.class));
        
        // When & Then
        mockMvc.perform(post("/api/v1/categories/relations")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isInternalServerError());
    }
    
    @Test
    void whenDeleteCategoryRelation_shouldReturnOk() throws Exception {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        String requestJson = objectMapper.writeValueAsString(relation);
        
        // When & Then
        mockMvc.perform(delete("/api/v1/categories/relations")
                .contentType("application/json")
                .content(requestJson))
            .andDo(print())
            .andExpect(status().isOk());
    }
    
    @Test
    void whenDeleteCategoryRelation_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        CategoryRelationDTO relation = CategoryRelationDTO.builder()
            .sourceId(1L)
            .targetId(2L)
            .build();
        
        String requestJson = objectMapper.writeValueAsString(relation);
        String errorMessage = "Delete error occurred";
        
        doThrow(new RuntimeException(errorMessage))
            .when(categoryService).deleteCategoryRelation(any(CategoryRelationDTO.class));
        
        // When & Then
        mockMvc.perform(delete("/api/v1/categories/relations")
                .contentType("application/json")
                .content(requestJson))
            .andExpect(status().isInternalServerError());
    }

    @Test
    void whenGetCategoryWithParents_shouldReturnCategoryWithParents() throws Exception {
        // Given
        Long id = 1L;
        CategoryDto parent1 = CategoryDto.builder().id(2L).name("Music").build();
        CategoryDto parent2 = CategoryDto.builder().id(3L).name("Genre").build();
        
        CategoryWithParentsDto expectedCategory = CategoryWithParentsDto.builder()
            .id(id)
            .name("Rock")
            .parents(Arrays.asList(parent1, parent2))
            .build();
        
        String expectedJson = objectMapper.writeValueAsString(expectedCategory);
        
        when(categoryService.getCategoryWithParents(id)).thenReturn(expectedCategory);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}/with-parents", id))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void whenGetCategoryWithParents_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        Long id = 1L;
        String errorMessage = "Category not found";
        
        when(categoryService.getCategoryWithParents(id))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/{id}/with-parents", id))
            .andExpect(status().isInternalServerError());
    }
}
