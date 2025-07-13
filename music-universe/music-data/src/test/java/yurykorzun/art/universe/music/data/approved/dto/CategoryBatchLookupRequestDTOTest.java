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

class CategoryBatchLookupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenNamesIsNull_thenValidationFails() {
        // given
        CategoryBatchLookupRequestDTO dto = CategoryBatchLookupRequestDTO.builder()
            .names(null)
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<CategoryBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertEquals(1, violations.size());
        ConstraintViolation<CategoryBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("names", violation.getPropertyPath().toString());
        assertEquals("Category names list cannot be empty", violation.getMessage());
    }

    @Test
    void whenNamesIsEmpty_thenValidationFails() {
        // given
        CategoryBatchLookupRequestDTO dto = CategoryBatchLookupRequestDTO.builder()
            .names(List.of())
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<CategoryBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertEquals(1, violations.size());
        ConstraintViolation<CategoryBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("names", violation.getPropertyPath().toString());
        assertEquals("Category names list cannot be empty", violation.getMessage());
    }

    @Test
    void whenNamesIsValid_thenValidationPasses() {
        // given
        CategoryBatchLookupRequestDTO dto = CategoryBatchLookupRequestDTO.builder()
            .names(List.of("rock", "jazz"))
            .limit(10)
            .build();

        // when
        Set<ConstraintViolation<CategoryBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty());
    }

    @Test
    void whenLimitIsNull_thenValidationPasses() {
        // given
        CategoryBatchLookupRequestDTO dto = CategoryBatchLookupRequestDTO.builder()
            .names(List.of("rock"))
            .limit(null)
            .build();

        // when
        Set<ConstraintViolation<CategoryBatchLookupRequestDTO>> violations = validator.validate(dto);

        // then
        assertTrue(violations.isEmpty());
    }
}
