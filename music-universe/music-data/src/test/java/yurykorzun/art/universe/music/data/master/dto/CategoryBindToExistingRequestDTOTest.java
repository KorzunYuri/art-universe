package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryBindToExistingRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenCategoryIdIsSet() {
        // Given
        Long categoryId = 1L;
        
        // When
        CategoryBindToExistingRequestDTO dto = CategoryBindToExistingRequestDTO.builder()
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
        CategoryBindToExistingRequestDTO dto = CategoryBindToExistingRequestDTO.builder()
            .categoryId(categoryId)
            .build();
        
        // Then
        assertEquals(categoryId, dto.getCategoryId());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        CategoryBindToExistingRequestDTO dto = new CategoryBindToExistingRequestDTO();
        dto.setCategoryId(456L);
        
        // Then
        assertEquals(456L, dto.getCategoryId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long categoryId = 789L;
        
        // When
        CategoryBindToExistingRequestDTO dto = new CategoryBindToExistingRequestDTO(categoryId);
        
        // Then
        assertEquals(categoryId, dto.getCategoryId());
    }
}
