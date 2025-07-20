package yurykorzun.art.universe.music.data.master.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArtistBindToExistingRequestDTOTest {

    @Test
    void shouldCreateValidDTO_whenArtistIdIsSet() {
        // Given
        Long artistId = 123L;
        
        // When
        ArtistBindToExistingRequestDTO dto = ArtistBindToExistingRequestDTO.builder()
            .artistId(artistId)
            .build();
        
        // Then
        assertEquals(artistId, dto.getArtistId());
    }

    @Test
    void shouldCreateDTOWithBuilder() {
        // Given
        Long artistId = 456L;
        
        // When
        ArtistBindToExistingRequestDTO dto = ArtistBindToExistingRequestDTO.builder()
            .artistId(artistId)
            .build();
        
        // Then
        assertEquals(artistId, dto.getArtistId());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistBindToExistingRequestDTO dto = new ArtistBindToExistingRequestDTO();
        dto.setArtistId(789L);
        
        // Then
        assertEquals(789L, dto.getArtistId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long artistId = 101L;
        
        // When
        ArtistBindToExistingRequestDTO dto = new ArtistBindToExistingRequestDTO(artistId);
        
        // Then
        assertEquals(artistId, dto.getArtistId());
    }
}
