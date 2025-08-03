package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistRelatedEntityCreateAndBindRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        String name = "Test Track";
        Long primaryArtistId = 123L;
        
        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(name)
            .primaryArtistId(primaryArtistId)
            .build();
        
        // Then
        assertEquals(name, dto.getEntityName());
        assertEquals(primaryArtistId, dto.getPrimaryArtistId());
    }

    @Test
    void shouldFailValidation_whenEntityNameIsNull() {
        // Given
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(null)
            .primaryArtistId(123L)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("entityName", violation.getPropertyPath().toString());
        assertEquals("Entity name is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenEntityNameIsEmpty() {
        // Given
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("")
            .primaryArtistId(1L)
            .build();

        // When
        Set<ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("entityName", violation.getPropertyPath().toString());
        assertEquals("Entity name is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenPrimaryArtistIdIsNull() {
        // Given
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Test Track")
            .primaryArtistId(null)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("primaryArtistId", violation.getPropertyPath().toString());
        assertEquals("Primary artist ID is required", violation.getMessage());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = new ArtistRelatedEntityCreateAndBindRequestDTO();
        dto.setEntityName("Test Track");
        dto.setPrimaryArtistId(123L);
        
        // Then
        assertEquals("Test Track", dto.getEntityName());
        assertEquals(123L, dto.getPrimaryArtistId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long primaryArtistId = 123L;
        
        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = new ArtistRelatedEntityCreateAndBindRequestDTO(primaryArtistId);
        dto.setEntityName("Test Track");
        
        // Then
        assertEquals("Test Track", dto.getEntityName());
        assertEquals(primaryArtistId, dto.getPrimaryArtistId());
    }
}
