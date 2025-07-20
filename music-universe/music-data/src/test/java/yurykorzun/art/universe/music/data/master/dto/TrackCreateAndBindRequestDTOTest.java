package yurykorzun.art.universe.music.data.master.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrackCreateAndBindRequestDTOTest {

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
        Long artistExternalId = 123L;
        
        // When
        TrackCreateAndBindRequestDTO dto = TrackCreateAndBindRequestDTO.builder()
            .name(name)
            .artistExternalId(artistExternalId)
            .build();
        
        // Then
        assertEquals(name, dto.getName());
        assertEquals(artistExternalId, dto.getArtistExternalId());
    }

    @Test
    void shouldFailValidation_whenNameIsNull() {
        // Given
        TrackCreateAndBindRequestDTO dto = TrackCreateAndBindRequestDTO.builder()
            .name(null)
            .artistExternalId(123L)
            .build();
        
        // When
        Set<ConstraintViolation<TrackCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<TrackCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Track name is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenNameIsEmpty() {
        // Given
        TrackCreateAndBindRequestDTO dto = TrackCreateAndBindRequestDTO.builder()
            .name("")
            .artistExternalId(123L)
            .build();
        
        // When
        Set<ConstraintViolation<TrackCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<TrackCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("name", violation.getPropertyPath().toString());
        assertEquals("Track name is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenArtistExternalIdIsNull() {
        // Given
        TrackCreateAndBindRequestDTO dto = TrackCreateAndBindRequestDTO.builder()
            .name("Test Track")
            .artistExternalId(null)
            .build();
        
        // When
        Set<ConstraintViolation<TrackCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<TrackCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("artistExternalId", violation.getPropertyPath().toString());
        assertEquals("Artist external ID is required", violation.getMessage());
    }
}
