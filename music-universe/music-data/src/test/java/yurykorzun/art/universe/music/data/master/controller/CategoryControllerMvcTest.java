package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.service.CategoryService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
class CategoryControllerMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CategoryService categoryService;

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
        
        when(categoryService.lookupCategories(searchQuery)).thenReturn(expectedCategories);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("name", searchQuery))
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
        
        when(categoryService.lookupCategories(searchQuery, limit)).thenReturn(expectedCategories);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("name", searchQuery)
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
        
        when(categoryService.lookupCategories(searchQuery)).thenReturn(emptyList);
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("name", searchQuery))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
    
    @Test
    void whenLookupCategories_withError_shouldReturnFailureResponse() throws Exception {
        // Given
        String searchQuery = "rock";
        String errorMessage = "Search error occurred";
        
        when(categoryService.lookupCategories(searchQuery))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("name", searchQuery))
            .andExpect(status().isInternalServerError());
    }
}
