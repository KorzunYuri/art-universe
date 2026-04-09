package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityBindToExistingRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenMasterIdIsSet() {
        // Given
        Long masterId = 123L;
        
        // When
        EntityBindToExistingRequestDTO dto = EntityBindToExistingRequestDTO.builder()
            .masterId(masterId)
            .build();
        
        // Then
        assertEquals(masterId, dto.getMasterId());
    }

    @Test
    void shouldFailValidation_whenMasterIdIsNull() {
        // Given
        EntityBindToExistingRequestDTO dto = EntityBindToExistingRequestDTO.builder()
            .masterId(null)
            .build();
        
        // When
        Set<ConstraintViolation<EntityBindToExistingRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EntityBindToExistingRequestDTO> violation = violations.iterator().next();
        assertEquals("masterId", violation.getPropertyPath().toString());
        assertEquals("Master entity ID is required", violation.getMessage());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        EntityBindToExistingRequestDTO dto = new EntityBindToExistingRequestDTO();
        dto.setMasterId(456L);
        
        // Then
        assertEquals(456L, dto.getMasterId());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        Long masterId = 789L;
        
        // When
        EntityBindToExistingRequestDTO dto = new EntityBindToExistingRequestDTO(masterId);
        
        // Then
        assertEquals(masterId, dto.getMasterId());
    }
}
