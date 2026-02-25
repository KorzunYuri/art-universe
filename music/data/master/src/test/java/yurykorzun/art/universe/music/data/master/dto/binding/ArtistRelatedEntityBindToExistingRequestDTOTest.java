package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistRelatedEntityBindToExistingRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        Long masterId = 123L;
        Long masterPrimaryArtistId = 456L;

        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .masterPrimaryArtistId(masterPrimaryArtistId)
            .build();

        // Then
        assertEquals(masterId, dto.getMasterId());
        assertEquals(masterPrimaryArtistId, dto.getMasterPrimaryArtistId());
    }

    @Test
    void shouldCreateValidDTO_whenMasterPrimaryArtistIdIsNull() {
        // Given — masterPrimaryArtistId is optional at DTO level; service validates it
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(123L)
            .build();

        // When
        Set<ConstraintViolation<ArtistRelatedEntityBindToExistingRequestDTO>> violations = validator.validate(dto);

        // Then — no violations (masterPrimaryArtistId is optional in DTO)
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldFailValidation_whenMasterIdIsNull() {
        // Given
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(null)
            .masterPrimaryArtistId(456L)
            .build();

        // When
        Set<ConstraintViolation<ArtistRelatedEntityBindToExistingRequestDTO>> violations = validator.validate(dto);

        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedEntityBindToExistingRequestDTO> violation = violations.iterator().next();
        assertEquals("masterId", violation.getPropertyPath().toString());
        assertEquals("Master entity ID is required", violation.getMessage());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = new ArtistRelatedEntityBindToExistingRequestDTO();
        dto.setMasterId(123L);
        dto.setMasterPrimaryArtistId(456L);

        // Then
        assertEquals(123L, dto.getMasterId());
        assertEquals(456L, dto.getMasterPrimaryArtistId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long masterPrimaryArtistId = 456L;

        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = new ArtistRelatedEntityBindToExistingRequestDTO(masterPrimaryArtistId);
        dto.setMasterId(123L);

        // Then
        assertEquals(123L, dto.getMasterId());
        assertEquals(masterPrimaryArtistId, dto.getMasterPrimaryArtistId());
    }
}
