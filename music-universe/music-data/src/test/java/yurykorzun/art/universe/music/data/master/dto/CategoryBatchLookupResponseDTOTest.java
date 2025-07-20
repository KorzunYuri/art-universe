package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CategoryBatchLookupResponseDTOTest {

    @Test
    void shouldCreateEmptyResponse_whenNoResultsProvided() {
        // When
        CategoryBatchLookupResponseDTO response = CategoryBatchLookupResponseDTO.builder().build();
        
        // Then
        assertNotNull(response.getResults());
        assertTrue(response.getResults().isEmpty());
    }
    
    @Test
    void shouldCreateResponse_withResults() {
        // Given
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("rock", List.of(
            new LookupResultDTO(1L, "Rock"),
            new LookupResultDTO(2L, "Alternative Rock")
        ));
        results.put("jazz", List.of(
            new LookupResultDTO(3L, "Jazz")
        ));
        
        // When
        CategoryBatchLookupResponseDTO response = CategoryBatchLookupResponseDTO.builder()
            .results(results)
            .build();
        
        // Then
        assertEquals(results, response.getResults());
        assertEquals(2, response.getResults().size());
        assertEquals(2, response.getResults().get("rock").size());
        assertEquals(1, response.getResults().get("jazz").size());
    }
    
    @Test
    void shouldCreateResponse_withNoArgsConstructor() {
        // When
        CategoryBatchLookupResponseDTO response = new CategoryBatchLookupResponseDTO();
        
        // Then
        assertNotNull(response.getResults());
        assertTrue(response.getResults().isEmpty());
        
        // When
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("rock", List.of(new LookupResultDTO(1L, "Rock")));
        response.setResults(results);
        
        // Then
        assertEquals(results, response.getResults());
    }
    
    @Test
    void shouldCreateResponse_withAllArgsConstructor() {
        // Given
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("rock", List.of(new LookupResultDTO(1L, "Rock")));
        
        // When
        CategoryBatchLookupResponseDTO response = new CategoryBatchLookupResponseDTO(results);
        
        // Then
        assertEquals(results, response.getResults());
    }
}
