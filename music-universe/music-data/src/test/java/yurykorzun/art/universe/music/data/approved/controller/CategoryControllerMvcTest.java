package yurykorzun.art.universe.music.data.approved.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.service.CategoryService;

import java.util.Collections;
import java.util.List;

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

    @Test
    void whenLookupCategories_shouldReturnMatchingCategories() throws Exception {
        // Given
        String searchQuery = "rock";
        LookupResultDTO category1 = new LookupResultDTO(1L, "Rock");
        LookupResultDTO category2 = new LookupResultDTO(2L, "Alternative Rock");
        List<LookupResultDTO> expectedCategories = List.of(category1, category2);
        
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedCategories));
        
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
        
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(expectedCategories));
        
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
        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(emptyList));
        
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
        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody(String.format("Failed to lookup categories: %s", errorMessage)));
        
        when(categoryService.lookupCategories(searchQuery))
            .thenThrow(new RuntimeException(errorMessage));
        
        // When & Then
        mockMvc.perform(get("/api/v1/categories/lookup")
                .param("name", searchQuery))
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));
    }
}
