package yurykorzun.art.universe.music.data.approved.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DimensionSaveRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenNameIsSet() {
        // Given
        String name = "Genre";
        
        // When
        DimensionSaveRequestDTO dto = DimensionSaveRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertNull(dto.getId());
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateValidDTO_whenIdAndNameAreSet() {
        // Given
        Long id = 1L;
        String name = "Updated Genre";
        
        // When
        DimensionSaveRequestDTO dto = DimensionSaveRequestDTO.builder()
            .id(id)
            .name(name)
            .build();
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithBuilder() {
        // Given
        Long id = 123L;
        String name = "Alternative Genre";
        
        // When
        DimensionSaveRequestDTO dto = DimensionSaveRequestDTO.builder()
            .id(id)
            .name(name)
            .build();
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        DimensionSaveRequestDTO dto = new DimensionSaveRequestDTO();
        dto.setId(456L);
        dto.setName("Jazz");
        
        // Then
        assertEquals(456L, dto.getId());
        assertEquals("Jazz", dto.getName());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long id = 789L;
        String name = "Electronic";
        
        // When
        DimensionSaveRequestDTO dto = new DimensionSaveRequestDTO(id, name);
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOForNewDimension_whenIdIsNull() {
        // Given
        String name = "New Dimension";
        
        // When
        DimensionSaveRequestDTO dto = DimensionSaveRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertNull(dto.getId());
        assertEquals(name, dto.getName());
    }
}
