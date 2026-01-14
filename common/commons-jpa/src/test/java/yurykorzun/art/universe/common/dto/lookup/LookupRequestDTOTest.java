package yurykorzun.art.universe.common.dto.lookup;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LookupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        String search = "test";
        Integer limit = 10;
        
        // When
        LookupRequestDTO dto = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();
        
        // Then
        assertEquals(search, dto.getSearch());
        assertEquals(limit, dto.getLimit());
    }

    @Test
    void shouldFailValidation_whenSearchIsNull() {
        // Given
        LookupRequestDTO dto = LookupRequestDTO.builder()
            .search(null)
            .limit(10)
            .build();
        
        // When
        Set<ConstraintViolation<LookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<LookupRequestDTO> violation = violations.iterator().next();
        assertEquals("search", violation.getPropertyPath().toString());
        assertEquals("Search term is required", violation.getMessage());
    }

    @Test
    void shouldCreateValidDTO_withOptionalFieldsNull() {
        // Given
        String search = "test";
        
        // When
        LookupRequestDTO dto = LookupRequestDTO.builder()
            .search(search)
            .limit(null)
            .build();
        
        // Then
        assertEquals(search, dto.getSearch());
        assertNull(dto.getLimit());
        
        // Validate
        Set<ConstraintViolation<LookupRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        LookupRequestDTO dto = new LookupRequestDTO();
        dto.setSearch("test");
        dto.setLimit(10);
        
        // Then
        assertEquals("test", dto.getSearch());
        assertEquals(10, dto.getLimit());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        String search = "test";
        Integer limit = 10;
        
        // When
        LookupRequestDTO dto = new LookupRequestDTO(search, limit);
        
        // Then
        assertEquals(search, dto.getSearch());
        assertEquals(limit, dto.getLimit());
    }
}
