package yurykorzun.art.universe.music.data.master.dto.lookup;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistRelatedBatchLookupRequestDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void shouldCreateValidDTO_whenAllFieldsAreSet() {
        // Given
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
            .search("paranoid")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
            .search("karma")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        List<ArtistRelatedLookupRequestDTO> searchRequests = Arrays.asList(request1, request2);
        Integer limit = 10;
        
        // When
        ArtistRelatedBatchLookupRequestDTO dto = ArtistRelatedBatchLookupRequestDTO.builder()
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
        ArtistRelatedBatchLookupRequestDTO dto = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(null)
            .limit(10)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedBatchLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedBatchLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("searchRequests", violation.getPropertyPath().toString());
        assertEquals("Search requests are required", violation.getMessage());
    }

    @Test
    void shouldCreateValidDTO_withOptionalFieldsNull() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("paranoid")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        List<ArtistRelatedLookupRequestDTO> searchRequests = List.of(request);
        
        // When
        ArtistRelatedBatchLookupRequestDTO dto = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(searchRequests)
            .limit(null)
            .build();
        
        // Then
        assertEquals(searchRequests, dto.getSearchRequests());
        assertNull(dto.getLimit());
        
        // Validate
        Set<ConstraintViolation<ArtistRelatedBatchLookupRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedBatchLookupRequestDTO dto = new ArtistRelatedBatchLookupRequestDTO();
        
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("paranoid")
            .masterArtistId(123L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        List<ArtistRelatedLookupRequestDTO> searchRequests = List.of(request);
        
        dto.setSearchRequests(searchRequests);
        dto.setLimit(10);
        
        // Then
        assertEquals(searchRequests, dto.getSearchRequests());
        assertEquals(10, dto.getLimit());
    }

    @Test
    void shouldValidateEmptySearchRequestsList() {
        // Given
        ArtistRelatedBatchLookupRequestDTO dto = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(Collections.emptyList())
            .limit(10)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedBatchLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertTrue(violations.isEmpty(), "Empty list should be valid, filtering will happen in service layer");
    }
}
