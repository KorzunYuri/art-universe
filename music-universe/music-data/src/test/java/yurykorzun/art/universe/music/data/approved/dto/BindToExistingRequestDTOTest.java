package yurykorzun.art.universe.music.data.approved.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BindToExistingRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenCategoryIdIsSet() {
        // Given
        Long categoryId = 1L;
        
        // When
        BindToExistingRequestDTO dto = BindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        // Then
        assertEquals(categoryId, dto.getCategoryId());
    }

    @Test
    void shouldCreateDTOWithBuilder() {
        // Given
        Long categoryId = 123L;
        
        // When
        BindToExistingRequestDTO dto = BindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        // Then
        assertEquals(categoryId, dto.getCategoryId());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        BindToExistingRequestDTO dto = new BindToExistingRequestDTO();
        dto.setCategoryId(456L);
        
        // Then
        assertEquals(456L, dto.getCategoryId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long categoryId = 789L;
        
        // When
        BindToExistingRequestDTO dto = new BindToExistingRequestDTO(categoryId);
        
        // Then
        assertEquals(categoryId, dto.getCategoryId());
    }
}
