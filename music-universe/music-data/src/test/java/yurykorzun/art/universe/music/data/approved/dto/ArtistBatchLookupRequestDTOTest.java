package yurykorzun.art.universe.music.data.approved.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtistBatchLookupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenSearchTermsIsNull_thenValidationFails() {
        // given
        ArtistBatchLookupRequestDTO dto = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(null)
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<ArtistBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("searchTerms", violation.getPropertyPath().toString());
        assertEquals("At least one search term is required", violation.getMessage());
    }

    @Test
    void whenSearchTermsIsEmpty_thenValidationFails() {
        // given
        ArtistBatchLookupRequestDTO dto = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(List.of())
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<ArtistBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("searchTerms", violation.getPropertyPath().toString());
        assertEquals("At least one search term is required", violation.getMessage());
    }

    @Test
    void whenSearchTermsIsValid_thenValidationPasses() {
        // given
        ArtistBatchLookupRequestDTO dto = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(List.of("radio", "queen"))
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<ArtistBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenLimitIsNull_thenValidationPasses() {
        // given
        ArtistBatchLookupRequestDTO dto = ArtistBatchLookupRequestDTO.builder()
            .searchTerms(List.of("radio"))
            .limit(null)
            .build();

        // when
        Set<ConstraintViolation<ArtistBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty());
    }
}
