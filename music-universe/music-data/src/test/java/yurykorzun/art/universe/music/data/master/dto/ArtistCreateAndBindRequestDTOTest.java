package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistCreateAndBindRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenNameIsSet() {
        // Given
        String name = "Radiohead";
        
        // When
        ArtistCreateAndBindRequestDTO dto = ArtistCreateAndBindRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithBuilder() {
        // Given
        String name = "Coldplay";
        
        // When
        ArtistCreateAndBindRequestDTO dto = ArtistCreateAndBindRequestDTO.builder()
            .name(name)
            .build();
        
        // Then
        assertEquals(name, dto.getName());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistCreateAndBindRequestDTO dto = new ArtistCreateAndBindRequestDTO();
        dto.setName("Muse");
        
        // Then
        assertEquals("Muse", dto.getName());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        String name = "Arctic Monkeys";
        
        // When
        ArtistCreateAndBindRequestDTO dto = new ArtistCreateAndBindRequestDTO(name);
        
        // Then
        assertEquals(name, dto.getName());
    }
}
