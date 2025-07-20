package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CategoryCreateAndBindRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenNameIsSet() {
        // Given
        String name = "Rock";
        
        // When
        CategoryCreateAndBindRequestDTO dto = CategoryCreateAndBindRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithBuilder() {
        // Given
        String name = "Alternative Rock";
        
        // When
        CategoryCreateAndBindRequestDTO dto = CategoryCreateAndBindRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        CategoryCreateAndBindRequestDTO dto = new CategoryCreateAndBindRequestDTO();
        dto.setName("Jazz");
        
        // Then
        assertEquals("Jazz", dto.getName());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        String name = "Electronic";
        
        // When
        CategoryCreateAndBindRequestDTO dto = new CategoryCreateAndBindRequestDTO(name);
        
        // Then
        assertEquals(name, dto.getName());
    }
}
