package yurykorzun.art.universe.common.dto.lookup;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BatchLookupResponseDTOTest {

    @Test
    void shouldCreateEmptyResponse_whenNoResultsProvided() {
        // When
        BatchLookupResponseDTO response = BatchLookupResponseDTO.builder().build();
        
        // Then
        assertNotNull(response.getResults());
        assertTrue(response.getResults().isEmpty());
    }
    
    @Test
    void shouldCreateResponse_withResults() {
        // Given
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("radio", List.of(
            new LookupResultDTO(1L, "Radiohead"),
            new LookupResultDTO(2L, "Radio Moscow")
        ));
        results.put("queen", List.of(
            new LookupResultDTO(3L, "Queen")
        ));
        
        // When
        BatchLookupResponseDTO response = BatchLookupResponseDTO.builder()
            .results(results)
            .build();
        
        // Then
        assertEquals(results, response.getResults());
        assertEquals(2, response.getResults().size());
        assertEquals(2, response.getResults().get("radio").size());
        assertEquals(1, response.getResults().get("queen").size());
    }
    
    @Test
    void shouldCreateResponse_withNoArgsConstructor() {
        // When
        BatchLookupResponseDTO response = new BatchLookupResponseDTO();
        
        // Then
        assertNotNull(response.getResults());
        assertTrue(response.getResults().isEmpty());
        
        // When
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("radio", List.of(new LookupResultDTO(1L, "Radiohead")));
        response.setResults(results);
        
        // Then
        assertEquals(results, response.getResults());
    }
    
    @Test
    void shouldCreateResponse_withAllArgsConstructor() {
        // Given
        Map<String, List<LookupResultDTO>> results = new HashMap<>();
        results.put("radio", List.of(new LookupResultDTO(1L, "Radiohead")));
        
        // When
        BatchLookupResponseDTO response = new BatchLookupResponseDTO(results);
        
        // Then
        assertEquals(results, response.getResults());
    }
}
