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
        Long masterPrimaryArtistId = 123L;

        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(name)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        // Then
        assertEquals(name, dto.getEntityName());
        assertEquals(masterPrimaryArtistId, dto.getMasterPrimaryArtistId());
    }

    @Test
    void shouldCreateValidDTO_whenMasterPrimaryArtistIdIsNull() {
        // Given — masterPrimaryArtistId is optional at DTO level; service validates it
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName("Test Track")
            .build();

        // When
        Set<ConstraintViolation<ArtistRelatedEntityCreateAndBindRequestDTO>> violations = validator.validate(dto);

        // Then — no violations (masterPrimaryArtistId is optional in DTO)
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_whenEntityNameIsNull() {
        // Given
        ArtistRelatedEntityCreateAndBindRequestDTO dto = ArtistRelatedEntityCreateAndBindRequestDTO.builder()
            .entityName(null)
            .masterPrimaryArtistId(123L)
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
            .masterPrimaryArtistId(1L)
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
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = new ArtistRelatedEntityCreateAndBindRequestDTO();
        dto.setEntityName("Test Track");
        dto.setMasterPrimaryArtistId(123L);

        // Then
        assertEquals("Test Track", dto.getEntityName());
        assertEquals(123L, dto.getMasterPrimaryArtistId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long masterPrimaryArtistId = 123L;

        // When
        ArtistRelatedEntityCreateAndBindRequestDTO dto = new ArtistRelatedEntityCreateAndBindRequestDTO(masterPrimaryArtistId);
        dto.setEntityName("Test Track");

        // Then
        assertEquals("Test Track", dto.getEntityName());
        assertEquals(masterPrimaryArtistId, dto.getMasterPrimaryArtistId());
    }
}
