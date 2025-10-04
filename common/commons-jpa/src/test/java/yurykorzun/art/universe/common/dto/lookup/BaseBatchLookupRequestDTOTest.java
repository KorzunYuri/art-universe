package yurykorzun.art.universe.common.dto.lookup;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class BaseBatchLookupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        List<LookupRequestDTO> searchRequests = Arrays.asList(
            LookupRequestDTO.builder().search("test1").limit(5).build(),
            LookupRequestDTO.builder().search("test2").limit(10).build()
        );
        Integer limit = 20;
        
        // When
        BaseBatchLookupRequestDTO dto = BaseBatchLookupRequestDTO.builder()
            .searchRequests(searchRequests)
            .limit(limit)
            .build();
        
        // Then
        assertEquals(searchRequests, dto.getSearchRequests());
        assertEquals(limit, dto.getLimit());
    }

    @Test
    void shouldFailValidation_whenSearchRequestsIsNull() {
        // Given
        BaseBatchLookupRequestDTO dto = BaseBatchLookupRequestDTO.builder()
            .searchRequests(null)
            .limit(20)
            .build();
        
        // When
        Set<ConstraintViolation<BaseBatchLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<BaseBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("searchRequests", violation.getPropertyPath().toString());
        assertEquals("Search requests are required", violation.getMessage());
    }

    @Test
    void shouldCreateValidDTO_withOptionalFieldsNull() {
        // Given
        List<LookupRequestDTO> searchRequests = Arrays.asList(
            LookupRequestDTO.builder().search("test1").build(),
            LookupRequestDTO.builder().search("test2").build()
        );
        
        // When
        BaseBatchLookupRequestDTO dto = BaseBatchLookupRequestDTO.builder()
            .searchRequests(searchRequests)
            .limit(null)
            .build();
        
        // Then
        assertEquals(searchRequests, dto.getSearchRequests());
        assertNull(dto.getLimit());
        
        // Validate
        Set<ConstraintViolation<BaseBatchLookupRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        BaseBatchLookupRequestDTO dto = new BaseBatchLookupRequestDTO();
        List<LookupRequestDTO> searchRequests = Arrays.asList(
            LookupRequestDTO.builder().search("test1").build(),
            LookupRequestDTO.builder().search("test2").build()
        );
        dto.setSearchRequests(searchRequests);
        dto.setLimit(20);
        
        // Then
        assertEquals(searchRequests, dto.getSearchRequests());
        assertEquals(20, dto.getLimit());
    }
    
    @Test
    void shouldValidateEmptySearchRequestsList() {
        // Given
        BaseBatchLookupRequestDTO dto = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Collections.emptyList())
            .limit(20)
            .build();
        
        // When
        Set<ConstraintViolation<BaseBatchLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertTrue(violations.isEmpty(), "Empty list should be valid, filtering will happen in service layer");
    }
}
