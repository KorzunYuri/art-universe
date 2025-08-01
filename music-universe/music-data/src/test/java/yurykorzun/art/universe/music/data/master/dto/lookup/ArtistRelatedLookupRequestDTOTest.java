package yurykorzun.art.universe.music.data.master.dto.lookup;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ArtistRelatedLookupRequestDTOTest {

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
        Long masterArtistId = 123L;
        Long externalArtistId = 456L;
        DataSource dataSource = DataSource.LASTFM;
        
        // When
        ArtistRelatedLookupRequestDTO dto = ArtistRelatedLookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .masterArtistId(masterArtistId)
            .externalArtistId(externalArtistId)
            .dataSource(dataSource)
            .build();
        
        // Then
        assertEquals(search, dto.getSearch());
        assertEquals(limit, dto.getLimit());
        assertEquals(masterArtistId, dto.getMasterArtistId());
        assertEquals(externalArtistId, dto.getExternalArtistId());
        assertEquals(dataSource, dto.getDataSource());
    }

    @Test
    void shouldFailValidation_whenSearchIsNull() {
        // Given
        ArtistRelatedLookupRequestDTO dto = ArtistRelatedLookupRequestDTO.builder()
            .search(null)
            .limit(10)
            .masterArtistId(123L)
            .externalArtistId(456L)
            .dataSource(DataSource.LASTFM)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("search", violation.getPropertyPath().toString());
        assertEquals("Search term is required", violation.getMessage());
    }

    @Test
    void shouldFailValidation_whenDataSourceIsNull() {
        // Given
        ArtistRelatedLookupRequestDTO dto = ArtistRelatedLookupRequestDTO.builder()
            .search("test")
            .limit(10)
            .masterArtistId(123L)
            .externalArtistId(456L)
            .dataSource(null)
            .build();
        
        // When
        Set<ConstraintViolation<ArtistRelatedLookupRequestDTO>> violations = validator.validate(dto);
        
        // Then
        assertEquals(1, violations.size());
        ConstraintViolation<ArtistRelatedLookupRequestDTO> violation = violations.iterator().next();
        assertEquals("dataSource", violation.getPropertyPath().toString());
        assertEquals("Data source is required", violation.getMessage());
    }

    @Test
    void shouldCreateValidDTO_withOptionalFieldsNull() {
        // Given
        String search = "test";
        DataSource dataSource = DataSource.LASTFM;
        
        // When
        ArtistRelatedLookupRequestDTO dto = ArtistRelatedLookupRequestDTO.builder()
            .search(search)
            .dataSource(dataSource)
            .limit(null)
            .masterArtistId(null)
            .externalArtistId(null)
            .build();
        
        // Then
        assertEquals(search, dto.getSearch());
        assertEquals(dataSource, dto.getDataSource());
        assertNull(dto.getLimit());
        assertNull(dto.getMasterArtistId());
        assertNull(dto.getExternalArtistId());
        
        // Validate
        Set<ConstraintViolation<ArtistRelatedLookupRequestDTO>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty());
    }

    @Test
    void shouldCreateDTOWithNoArgsConstructor() {
        // When
        ArtistRelatedLookupRequestDTO dto = new ArtistRelatedLookupRequestDTO();
        dto.setSearch("test");
        dto.setLimit(10);
        dto.setMasterArtistId(123L);
        dto.setExternalArtistId(456L);
        dto.setDataSource(DataSource.LASTFM);
        
        // Then
        assertEquals("test", dto.getSearch());
        assertEquals(10, dto.getLimit());
        assertEquals(123L, dto.getMasterArtistId());
        assertEquals(456L, dto.getExternalArtistId());
        assertEquals(DataSource.LASTFM, dto.getDataSource());
    }

    @Test
    void shouldCreateDTOWithAllArgsConstructor() {
        // Given
        String search = "test";
        Integer limit = 10;
        Long masterArtistId = 123L;
        Long externalArtistId = 456L;
        DataSource dataSource = DataSource.LASTFM;
        
        // When
        ArtistRelatedLookupRequestDTO dto = new ArtistRelatedLookupRequestDTO(masterArtistId, dataSource, externalArtistId);
        dto.setSearch(search);
        dto.setLimit(limit);
        dto.setDataSource(dataSource);
        
        // Then
        assertEquals(search, dto.getSearch());
        assertEquals(limit, dto.getLimit());
        assertEquals(masterArtistId, dto.getMasterArtistId());
        assertEquals(externalArtistId, dto.getExternalArtistId());
        assertEquals(dataSource, dto.getDataSource());
    }
}
