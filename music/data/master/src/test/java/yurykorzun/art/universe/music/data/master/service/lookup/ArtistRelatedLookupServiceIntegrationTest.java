package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.repository.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ArtistRelatedLookupServiceIntegrationTest extends BaseMasterDataJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistBindingRepository artistBindingRepository;

    private ArtistRelatedLookupService trackLookupService;
    private ArtistRelatedLookupService albumLookupService;

    private Artist radiohead;
    private Artist nirvana;
    private Artist jayChow;
    private final Long radioHeadExternalId = 1001L;
    private final Long nirvanaExternalId = 1002L;

    @BeforeEach
    void setUp() {
        trackLookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.TRACK);
        albumLookupService = new ArtistRelatedLookupService(entityManager, MasterEntityType.ALBUM);

        // Clear existing data
        trackRepository.deleteAll();
        albumRepository.deleteAll();
        artistBindingRepository.deleteAll();
        artistRepository.deleteAll();

        // Create test artists
        radiohead = artistRepository.save(Artist.builder().name("Radiohead").build());
        nirvana = artistRepository.save(Artist.builder().name("Nirvana").build());
        jayChow = artistRepository.save(Artist.builder().name("周杰倫").build()); // Jay Chou in Chinese

        // Create artist bindings
        artistBindingRepository.save(ArtistBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(radioHeadExternalId)
            .masterId(radiohead.getId())
            .build());

        artistBindingRepository.save(ArtistBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(nirvanaExternalId)
            .masterId(nirvana.getId())
            .build());

        // Create test tracks
        trackRepository.saveAll(Arrays.asList(
            Track.builder().name("Paranoid Android").primaryArtistId(radiohead.getId()).build(),
            Track.builder().name("Karma Police").primaryArtistId(radiohead.getId()).build(),
            Track.builder().name("No Surprises").primaryArtistId(radiohead.getId()).build(),
            Track.builder().name("Smells Like Teen Spirit").primaryArtistId(nirvana.getId()).build(),
            Track.builder().name("Come As You Are").primaryArtistId(nirvana.getId()).build(),
            Track.builder().name("Lithium").primaryArtistId(nirvana.getId()).build(),
            Track.builder().name("青花瓷").primaryArtistId(jayChow.getId()).build(), // Chinese track name
            Track.builder().name("稻香").primaryArtistId(jayChow.getId()).build()    // Chinese track name
        ));

        // Create test albums
        albumRepository.saveAll(Arrays.asList(
            Album.builder().name("OK Computer").primaryArtistId(radiohead.getId()).build(),
            Album.builder().name("Kid A").primaryArtistId(radiohead.getId()).build(),
            Album.builder().name("Nevermind").primaryArtistId(nirvana.getId()).build(),
            Album.builder().name("In Utero").primaryArtistId(nirvana.getId()).build(),
            Album.builder().name("七里香").primaryArtistId(jayChow.getId()).build() // Chinese album name
        ));
    }

    @Test
    void lookup_withValidSearchTerm_shouldReturnMatchingResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("paranoid")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("Radiohead - Paranoid Android");
    }

    @Test
    void lookup_withMasterArtistId_shouldFilterByArtist() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("a")  // Very generic search to match multiple tracks
            .masterArtistId(nirvana.getId())
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name")
            .contains("Nirvana - Come As You Are")
            .doesNotContain("Radiohead - Paranoid Android");
    }

    @Test
    void lookup_withExternalArtistId_shouldFilterByArtist() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("a")  // Very generic search to match multiple tracks
            .externalArtistId(radioHeadExternalId)  // Radiohead's external ID
            .dataSource(DataSource.LASTFM)
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name")
            .contains("Radiohead - Paranoid Android")
            .doesNotContain("Nirvana - Come As You Are");
    }

    @Test
    void lookup_withNonLatinScript_shouldReturnMatchingResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("青花")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name").contains("周杰倫 - 青花瓷");
    }

    @Test
    void lookup_withEmptySearchTerm_withoutArtistId_shouldReturnEmptyList() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("")
            .dataSource(DataSource.LASTFM)
            .limit(20)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withNullSearchTerm_withoutArtistId_shouldReturnEmptyList() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search(null)
            .dataSource(DataSource.LASTFM)
            .limit(20)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withEmptySearchTerm_withMasterArtistId_shouldReturnResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("")
            .masterArtistId(radiohead.getId())
            .dataSource(DataSource.LASTFM)
            .limit(20)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name")
            .contains("Radiohead - Paranoid Android")
            .contains("Radiohead - Karma Police")
            .contains("Radiohead - No Surprises")
            .doesNotContain("Nirvana - Smells Like Teen Spirit");
    }

    @Test
    void lookup_withNullSearchTerm_withMasterArtistId_shouldReturnResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search(null)
            .masterArtistId(nirvana.getId())
            .dataSource(DataSource.LASTFM)
            .limit(20)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name")
            .contains("Nirvana - Smells Like Teen Spirit")
            .contains("Nirvana - Come As You Are")
            .contains("Nirvana - Lithium")
            .doesNotContain("Radiohead - Paranoid Android");
    }

    @Test
    void lookup_withEmptySearchTerm_withExternalArtistId_shouldReturnResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("")
            .externalArtistId(radioHeadExternalId)
            .dataSource(DataSource.LASTFM)
            .limit(20)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).extracting("name")
            .contains("Radiohead - Paranoid Android")
            .contains("Radiohead - Karma Police")
            .contains("Radiohead - No Surprises")
            .doesNotContain("Nirvana - Smells Like Teen Spirit");
    }

    @Test
    void lookup_withNoMatchingResults_shouldReturnEmptyList() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("nonexistenttrack")
            .limit(10)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).isEmpty();
    }

    @Test
    void lookup_withLimitLessThanResults_shouldLimitResults() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("")  // Empty search to match all
            .dataSource(DataSource.LASTFM)
            .externalArtistId(nirvanaExternalId)
            .limit(2)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).hasSize(2);
    }

    @Test
    void lookup_withNullLimit_shouldUseDefaultLimit() {
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search("")  // Empty search to match all
            .limit(null)
            .build();

        // When
        List<LookupResultDTO> results = trackLookupService.lookup(request);

        // Then
        assertThat(results).hasSizeLessThanOrEqualTo(20); // Default limit is 20
    }

    @Test
    void batchLookup_withValidRequests_shouldReturnMatchingResults() {
        // Given
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
            .search("paranoid")
            .limit(10)
            .build();

        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
            .search("smells")
            .limit(10)
            .build();

        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = trackLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get("paranoid")).extracting("name").contains("Radiohead - Paranoid Android");
        assertThat(response.getResults().get("smells")).extracting("name").contains("Nirvana - Smells Like Teen Spirit");
    }

    @Test
    void batchLookup_withMixedArtistFilters_shouldReturnCorrectResults() {
        final String search1 = "";
        final String search2 = " ";
        // Given
        ArtistRelatedLookupRequestDTO request1 = ArtistRelatedLookupRequestDTO.builder()
            .search(search1)  // Empty search to match all
            .masterArtistId(radiohead.getId())
            .limit(10)
            .build();

        ArtistRelatedLookupRequestDTO request2 = ArtistRelatedLookupRequestDTO.builder()
            .search(search2)  // Empty search to match all
            .externalArtistId(nirvanaExternalId)  // Nirvana's external ID
            .dataSource(DataSource.LASTFM)
            .limit(10)
            .build();

        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(Arrays.asList(request1, request2))
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = trackLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).hasSize(2);
        
        // First request should return only Radiohead tracks
        List<LookupResultDTO> actual1 = response.getResults().get(search1);
        assertNotNull(actual1);
        assertThat(actual1).extracting("name")
            .contains("Radiohead - Paranoid Android")
            .contains("Radiohead - Karma Police")
            .doesNotContain("Nirvana - Smells Like Teen Spirit");
        
        // Second request should return only Nirvana tracks
        List<LookupResultDTO> actual2 = response.getResults().get(search2);
        assertNotNull(actual2);
        assertThat(actual2).extracting("name")
            .contains("Nirvana - Smells Like Teen Spirit")
            .contains("Nirvana - Come As You Are")
            .doesNotContain("Radiohead - Paranoid Android");
    }

    @Test
    void batchLookup_withEmptyRequests_shouldReturnEmptyResults() {
        // Given
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(Collections.emptyList())
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = trackLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).isEmpty();
    }

    @Test
    void batchLookup_withNullRequests_shouldReturnEmptyResults() {
        // Given
        ArtistRelatedBatchLookupRequestDTO batchRequest = ArtistRelatedBatchLookupRequestDTO.builder()
            .searchRequests(null)
            .limit(10)
            .build();

        // When
        BatchLookupResponseDTO response = trackLookupService.batchLookup(batchRequest);

        // Then
        assertThat(response.getResults()).isEmpty();
    }

    @ParameterizedTest
    @MethodSource("provideSearchAndArtistCombinations")
    void lookup_withParameterizedInput_shouldHandleAllCases(
            String searchTerm, 
            Long masterArtistId, 
            Long externalArtistId,
            MasterEntityType entityType) {
        
        // Given
        ArtistRelatedLookupRequestDTO request = ArtistRelatedLookupRequestDTO.builder()
            .search(searchTerm)
            .masterArtistId(masterArtistId)
            .externalArtistId(externalArtistId)
            .dataSource(DataSource.LASTFM) // cannot be null, covered by validation
            .limit(10)
            .build();

        ArtistRelatedLookupService service = entityType == MasterEntityType.TRACK ? trackLookupService : albumLookupService;

        // When
        List<LookupResultDTO> results = service.lookup(request);

        // Then
        // We don't assert on specific results, just that the method executes without errors
        assertThat(results).isNotNull();
    }

    private static Stream<Arguments> provideSearchAndArtistCombinations() {
        return Stream.of(
            // Test all combinations of search term, masterArtistId, externalArtistId for tracks
            // Valid search term
            Arguments.of("a",   null,   null,   MasterEntityType.TRACK),
            Arguments.of("a",   1L,     null,   MasterEntityType.TRACK),
            Arguments.of("a",   null,   1001L,  MasterEntityType.TRACK),
            Arguments.of("a",   1L,     1001L,  MasterEntityType.TRACK),
            
            // Empty search term
            Arguments.of("",    null,   null,   MasterEntityType.TRACK),
            Arguments.of("",    1L,     null,   MasterEntityType.TRACK),
            Arguments.of("",    null,   1001L,  MasterEntityType.TRACK),
            Arguments.of("",    1L,     1001L,  MasterEntityType.TRACK),
            
            // Null search term
            Arguments.of(null,  null,   null,   MasterEntityType.TRACK),
            Arguments.of(null,  1L,     null,   MasterEntityType.TRACK),
            Arguments.of(null,  null,   1001L,  MasterEntityType.TRACK),
            Arguments.of(null,  1L,     1001L,  MasterEntityType.TRACK),

            // Same combinations for albums
            Arguments.of("a",   null,   null,   MasterEntityType.ALBUM),
            Arguments.of("a",   1L,     null,   MasterEntityType.ALBUM),
            Arguments.of("a",   null,   1001L,  MasterEntityType.ALBUM),
            Arguments.of("",    null,   null,   MasterEntityType.ALBUM),
            Arguments.of(null,  1L,     null,   MasterEntityType.ALBUM),
            Arguments.of(null,  null,   1001L,  MasterEntityType.ALBUM)
        );
    }
}
