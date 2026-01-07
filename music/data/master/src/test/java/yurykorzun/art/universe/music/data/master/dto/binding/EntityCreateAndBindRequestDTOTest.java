package yurykorzun.art.universe.music.data.master.dto.binding;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class EntityCreateAndBindRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenNameIsSet() {
        // Given
        String name = "Test Entity";
        
        // When
        EntityCreateAndBindRequestDTO dto = EntityCreateAndBindRequestDTO.builder()
            .entityName(name)
            .build();
        
        // Then
        assertEquals(name, dto.getEntityName());
    }

    @Test
    void shouldFailValidation_whenNameIsNull() {
        // Given
        EntityCreateAndBindRequestDTO dto = EntityCreateAndBindRequestDTO.builder()
            .entityName(null)
            .build();
        
        // When
        Set<ConstraintViolation<EntityCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EntityCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("entityName", violation.getPropertyPath().toString());
        assertEquals("Entity name is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenEntityNameIsEmpty() {
        // Given
        EntityCreateAndBindRequestDTO dto = EntityCreateAndBindRequestDTO.builder()
            .entityName("")
            .build();
        
        // When
        Set<ConstraintViolation<EntityCreateAndBindRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<EntityCreateAndBindRequestDTO> violation = violations.iterator().next();
        assertEquals("entityName", violation.getPropertyPath().toString());
        assertEquals("Entity name is required", violation.getMessage());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        EntityCreateAndBindRequestDTO dto = new EntityCreateAndBindRequestDTO();
        dto.setEntityName("Test Entity");
        
        // Then
        assertEquals("Test Entity", dto.getEntityName());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        String name = "Test Entity";
        
        // When
        EntityCreateAndBindRequestDTO dto = new EntityCreateAndBindRequestDTO(name);
        
        // Then
        assertEquals(name, dto.getEntityName());
    }
}
