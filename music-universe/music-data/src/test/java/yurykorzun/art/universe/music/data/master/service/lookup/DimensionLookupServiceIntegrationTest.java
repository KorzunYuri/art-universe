package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.master.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.Dimension;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.repository.DimensionRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
class DimensionLookupServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DimensionRepository dimensionRepository;

    private DimensionLookupService lookupService;

    @BeforeEach
    void setUp() {
        lookupService = new DimensionLookupService(entityManager, EntityType.DIMENSION);

        // Clear existing data
        dimensionRepository.deleteAll();

        // Create test dimensions
        dimensionRepository.saveAll(Arrays.asList(
            Dimension.builder().name("Genre").build(),
            Dimension.builder().name("Mood").build(),
            Dimension.builder().name("Era").build(),
            Dimension.builder().name("Tempo").build(),
            Dimension.builder().name("Instrument").build(),
            Dimension.builder().name("Region").build(),
            // Add dimension with special characters and non-Latin script
            Dimension.builder().name("风格").build(), // Style in Chinese
            Dimension.builder().name("Époque").build() // Era in French
        ));
    }

    @Test
    void lookup_withValidSearchTerm_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("gen")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("Genre");
    }

    @Test
    void lookup_withNonLatinScript_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("风")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("风格");
    }

    @Test
    void lookup_withSpecialCharacters_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("Épo")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("Époque");
    }

    @Test
    void lookup_withEmptySearchTerm_shouldReturnAllResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(8); // All dimensions
    }

    @Test
    void lookup_withNullSearchTerm_shouldReturnAllResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(null)
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(8); // All dimensions
    }

    @Test
    void lookup_withNoMatchingResults_shouldReturnEmptyList() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("nonexistentdimension")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withLimitLessThanResults_shouldLimitResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("")  // Empty search to match all
            .limit(3)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).hasSize(3);
    }

    @Test
    void lookup_withNullLimit_shouldUseDefaultLimit() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("")  // Empty search to match all
            .limit(null)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        assertThat(results).hasSizeLessThanOrEqualTo(20); // Default limit is 20
    }

    @Test
    void batchLookup_withValidRequests_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
            .search("gen")
            .limit(10)
            .build();

        LookupRequestDTO request2 = LookupRequestDTO.builder()
            .search("mood")
            .limit(10)
            .build();

        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = lookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get("gen")).extracting("name").contains("Genre");
        assertThat(response.getResults().get("mood")).extracting("name").contains("Mood");
    }

    @Test
    void batchLookup_withMixedSearchTerms_shouldHandleEmptyAndNonEmpty() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
            .search("gen")
            .limit(5)
            .build();
        
        LookupRequestDTO request2 = LookupRequestDTO.builder()
            .search("")
            .limit(3)
            .build();
        
        LookupRequestDTO request3 = LookupRequestDTO.builder()
            .search(" ")
            .limit(2)
            .build();
        
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2, request3))
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = lookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).hasSize(3);
        assertThat(response.getResults().get("gen")).extracting("name").contains("Genre");
        assertThat(response.getResults().get("")).hasSize(3); // Limited to 3
        assertThat(response.getResults().get(" ")).hasSize(2); // Limited to 2
    }

    @Test
    void batchLookup_withEmptyRequests_shouldReturnEmptyResults() {
        // Given
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Collections.emptyList())
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = lookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).isEmpty();
    }

    @Test
    void batchLookup_withNullRequests_shouldReturnEmptyResults() {
        // Given
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(null)
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = lookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideSearchTermsAndLimits")
    void lookup_withParameterizedInput_shouldHandleAllCases(String searchTerm, Integer limit) {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(searchTerm)
            .limit(limit)
            .build();

        // When
        List<LookupResultDTO> results = lookupService.lookup(request);

        // Then
        // We don't assert on specific results, just that the method executes without errors
        assertThat(results).isNotNull();
        
        // Empty or null search should return all results (up to limit)
        if (searchTerm == null || searchTerm.isEmpty()) {
            if (limit != null && limit > 0) {
                assertThat(results.size()).isLessThanOrEqualTo(limit);
            }
            // If limit is null, default limit (20) should be used
            if (limit == null) {
                assertThat(results.size()).isLessThanOrEqualTo(20);
            }
        }
    }

    @Test
    void lookup_withZeroLimit_shouldThrowException() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("test")
            .limit(0)
            .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> lookupService.lookup(request));
    }

    @Test
    void lookup_withNegativeLimit_shouldThrowException() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("test")
            .limit(-1)
            .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> lookupService.lookup(request));
    }

    private static Stream<Arguments> provideSearchTermsAndLimits() {
        return Stream.of(
            // Valid search term with various limits - positive cases only
            Arguments.of("gen", 5),
            Arguments.of("gen", null),
            Arguments.of("gen", 100),
            
            // Empty search term with various limits - positive cases only
            Arguments.of("", 5),
            Arguments.of("", null),
            Arguments.of("", 100),
            
            // Null search term with various limits - positive cases only
            Arguments.of(null, 5),
            Arguments.of(null, null),
            Arguments.of(null, 100),
            
            // Non-existent search term
            Arguments.of("nonexistent", 5),
            Arguments.of("nonexistent", null)
        );
    }
}
