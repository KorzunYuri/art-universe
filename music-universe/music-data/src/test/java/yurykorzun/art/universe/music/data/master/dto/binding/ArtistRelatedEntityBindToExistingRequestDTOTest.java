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
        Long primaryArtistId = 456L;
        
        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .primaryArtistId(primaryArtistId)
            .build();
        
        // Then
        assertEquals(masterId, dto.getMasterId());
        assertEquals(primaryArtistId, dto.getPrimaryArtistId());
    }

    @Test
    void shouldFailValidation_whenMasterIdIsNull() {
        // Given
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(null)
            .primaryArtistId(456L)
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
    void shouldFailValidation_whenPrimaryArtistIdIsNull() {
        // Given
        ArtistRelatedEntityBindToExistingRequestDTO dto = ArtistRelatedEntityBindToExistingRequestDTO.builder()
            .masterId(123L)
            .primaryArtistId(null)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedEntityBindToExistingRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedEntityBindToExistingRequestDTO> violation = violations.iterator().next();
        assertEquals("primaryArtistId", violation.getPropertyPath().toString());
        assertEquals("Primary artist ID is required", violation.getMessage());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = new ArtistRelatedEntityBindToExistingRequestDTO();
        dto.setMasterId(123L);
        dto.setPrimaryArtistId(456L);
        
        // Then
        assertEquals(123L, dto.getMasterId());
        assertEquals(456L, dto.getPrimaryArtistId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long primaryArtistId = 456L;
        
        // When
        ArtistRelatedEntityBindToExistingRequestDTO dto = new ArtistRelatedEntityBindToExistingRequestDTO(primaryArtistId);
        dto.setMasterId(123L);
        
        // Then
        assertEquals(123L, dto.getMasterId());
        assertEquals(primaryArtistId, dto.getPrimaryArtistId());
    }
}
