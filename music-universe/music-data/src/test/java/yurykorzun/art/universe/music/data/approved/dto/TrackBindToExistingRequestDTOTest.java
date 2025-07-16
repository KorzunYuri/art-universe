package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TrackBindToExistingRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        Long trackId = 123L;
        Long artistExternalId = 456L;
        
        // When
        TrackBindToExistingRequestDTO dto = TrackBindToExistingRequestDTO.builder()
            .trackId(trackId)
            .artistExternalId(artistExternalId)
            .build();
        
        // Then
        assertEquals(trackId, dto.getTrackId());
        assertEquals(artistExternalId, dto.getArtistExternalId());
    }

    @Test
    void shouldFailValidation_whenTrackIdIsNull() {
        // Given
        TrackBindToExistingRequestDTO dto = TrackBindToExistingRequestDTO.builder()
            .trackId(null)
            .artistExternalId(456L)
            .build();
        
        // When
        Set<ConstraintViolation<TrackBindToExistingRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<TrackBindToExistingRequestDTO> violation = violations.iterator().next();
        assertEquals("trackId", violation.getPropertyPath().toString());
        assertEquals("Track ID is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenArtistExternalIdIsNull() {
        // Given
        TrackBindToExistingRequestDTO dto = TrackBindToExistingRequestDTO.builder()
            .trackId(123L)
            .artistExternalId(null)
            .build();
        
        // When
        Set<ConstraintViolation<TrackBindToExistingRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<TrackBindToExistingRequestDTO> violation = violations.iterator().next();
        assertEquals("artistExternalId", violation.getPropertyPath().toString());
        assertEquals("Artist external ID is required", violation.getMessage());
    }
}
