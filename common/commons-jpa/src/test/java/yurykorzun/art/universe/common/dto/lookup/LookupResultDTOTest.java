package yurykorzun.art.universe.common.dto.lookup;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;

import static org.junit.jupiter.api.Assertions.*;

class LookupResultDTOTest {

    @Test
    void shouldCreateDTO_withBuilder() {
        // Given
        Long id = 1L;
        String name = "Test Name";
        
        // When
        LookupResultDTO dto = LookupResultDTO.builder()
            .id(id)
            .name(name)
            .build();
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
    }
    
    @Test
    void shouldCreateDTO_withNoArgsConstructor() {
        // When
        LookupResultDTO dto = new LookupResultDTO();
        dto.setId(1L);
        dto.setName("Test Name");
        
        // Then
        assertEquals(1L, dto.getId());
        assertEquals("Test Name", dto.getName());
    }
    
    @Test
    void shouldCreateDTO_withAllArgsConstructor() {
        // Given
        Long id = 1L;
        String name = "Test Name";
        
        // When
        LookupResultDTO dto = new LookupResultDTO(id, name);
        
        // Then
        assertEquals(id, dto.getId());
        assertEquals(name, dto.getName());
    }
    
    @Test
    void shouldImplementEqualsAndHashCode() {
        // Given
        LookupResultDTO dto1 = new LookupResultDTO(1L, "Test Name");
        LookupResultDTO dto2 = new LookupResultDTO(1L, "Test Name");
        LookupResultDTO dto3 = new LookupResultDTO(2L, "Test Name");
        
        // Then
        assertEquals(dto1, dto2);
        assertNotEquals(dto1, dto3);
        assertEquals(dto1.hashCode(), dto2.hashCode());
        assertNotEquals(dto1.hashCode(), dto3.hashCode());
    }
    
    @Test
    void shouldImplementToString() {
        // Given
        LookupResultDTO dto = new LookupResultDTO(1L, "Test Name");
        
        // When
        String toString = dto.toString();
        
        // Then
        assertTrue(toString.contains("id=1"));
        assertTrue(toString.contains("name=Test Name"));
    }
}
