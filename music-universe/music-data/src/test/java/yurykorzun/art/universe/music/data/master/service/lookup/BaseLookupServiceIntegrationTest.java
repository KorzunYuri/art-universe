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
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.repository.ArtistRepository;
import yurykorzun.art.universe.music.data.master.repository.CategoryRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
class BaseLookupServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private BaseLookupService artistLookupService;
    private BaseLookupService categoryLookupService;

    @BeforeEach
    void setUp() {
        artistLookupService = new BaseLookupService(entityManager, EntityType.ARTIST);
        categoryLookupService = new BaseLookupService(entityManager, EntityType.CATEGORY);

        // Clear existing data
        artistRepository.deleteAll();
        categoryRepository.deleteAll();

        // Create test data
        artistRepository.saveAll(Arrays.asList(
            Artist.builder().name("Radiohead").build(),
            Artist.builder().name("Nirvana").build(),
            Artist.builder().name("Pink Floyd").build(),
            Artist.builder().name("The Beatles").build(),
            Artist.builder().name("Queen").build(),
            Artist.builder().name("Led Zeppelin").build(),
            Artist.builder().name("Metallica").build(),
            Artist.builder().name("AC/DC").build(),
            Artist.builder().name("The Rolling Stones").build(),
            Artist.builder().name("The Who").build(),
            // Add artist with special characters and non-Latin script
            Artist.builder().name("Björk").build(),
            Artist.builder().name("Sigur Rós").build(),
            Artist.builder().name("周杰倫").build() // Jay Chou in Chinese
        ));

        categoryRepository.saveAll(Arrays.asList(
            Category.builder().name("Rock").build(),
            Category.builder().name("Alternative").build(),
            Category.builder().name("Metal").build(),
            Category.builder().name("Pop").build(),
            Category.builder().name("Electronic").build(),
            Category.builder().name("Classical").build(),
            Category.builder().name("Jazz").build(),
            Category.builder().name("Hip Hop").build(),
            Category.builder().name("R&B").build(),
            Category.builder().name("Country").build(),
            // Add category with special characters and non-Latin script
            Category.builder().name("民謠").build(), // Folk in Chinese
            Category.builder().name("Électronique").build()
        ));
    }

    @Test
    void lookup_withValidSearchTerm_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("radio")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("Radiohead");
    }

    @Test
    void lookup_withNonLatinScript_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("周")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("周杰倫");
    }

    @Test
    void lookup_withSpecialCharacters_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("Björk")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("Björk");
    }

    @Test
    void lookup_withEmptySearchTerm_shouldReturnEmptyList() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withNullSearchTerm_shouldReturnEmptyList() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(null)
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withNoMatchingResults_shouldReturnEmptyList() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("nonexistentartist")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withLimitLessThanResults_shouldLimitResults() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("the")
            .limit(2)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void lookup_withNullLimit_shouldUseDefaultLimit() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("a")
            .limit(null)
            .build();

        // When
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertThat(results).hasSizeLessThanOrEqualTo(20); // Default limit is 20
    }

    @Test
    void batchLookup_withValidRequests_shouldReturnMatchingResults() {
        // Given
        LookupRequestDTO request1 = LookupRequestDTO.builder()
            .search("radio")
            .limit(10)
            .build();

        LookupRequestDTO request2 = LookupRequestDTO.builder()
            .search("metal")
            .limit(10)
            .build();

        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = artistLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get("radio")).extracting("name").contains("Radiohead");
        assertThat(response.getResults().get("metal")).extracting("name").contains("Metallica");
    }

    @Test
    void batchLookup_withEmptyRequests_shouldReturnEmptyResults() {
        // Given
        BaseBatchLookupRequestDTO batchRequest = BaseBatchLookupRequestDTO.builder()
            .searchRequests(Collections.emptyList())
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = artistLookupService.batchLookup(batchRequest);

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
        BatchLookupResponseDTO response = artistLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideSearchTermsAndLimits")
    void lookup_withParameterizedInput_shouldHandleAllCases(String searchTerm, Integer limit, EntityType entityType) {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(searchTerm)
            .limit(limit)
            .build();

        BaseLookupService service = entityType == EntityType.ARTIST ? artistLookupService : categoryLookupService;

        // When
        List<LookupResultDTO> results = service.lookup(request);

        // Then
        if (searchTerm == null || searchTerm.isEmpty()) {
            assertThat(results).isEmpty();
        } else {
            // We don't assert on specific results, just that the method executes without errors
            assertThat(results).isNotNull();
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
        assertThrows(IllegalArgumentException.class, () -> artistLookupService.lookup(request));
    }

    @Test
    void lookup_withNegativeLimit_shouldThrowException() {
        // Given
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("test")
            .limit(-1)
            .build();

        // When/Then
        assertThrows(IllegalArgumentException.class, () -> artistLookupService.lookup(request));
    }

    private static Stream<Arguments> provideSearchTermsAndLimits() {
        return Stream.of(
            // Test with artist lookup service - positive cases only
            Arguments.of("a", 5, EntityType.ARTIST),
            Arguments.of("a", null, EntityType.ARTIST),
            Arguments.of("", 5, EntityType.ARTIST),
            Arguments.of(null, 5, EntityType.ARTIST),
            Arguments.of("nonexistent", 5, EntityType.ARTIST),
            
            // Test with category lookup service - positive cases only
            Arguments.of("rock", 5, EntityType.CATEGORY),
            Arguments.of("rock", null, EntityType.CATEGORY),
            Arguments.of("", 5, EntityType.CATEGORY),
            Arguments.of(null, 5, EntityType.CATEGORY),
            Arguments.of("nonexistent", 5, EntityType.CATEGORY)
        );
    }
}
