package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import yurykorzun.art.universe.music.data.master.common.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.*;
import yurykorzun.art.universe.music.data.master.repository.*;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Import(
    BindingServiceImpl.class
)
class BindingServiceImplIntegrationTest extends BaseMasterDataJpaTest {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private BindingService bindingService;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ArtistBindingRepository artistBindingRepository;

    @Autowired
    private CategoryBindingRepository categoryBindingRepository;

    @Autowired
    private TrackBindingRepository trackBindingRepository;

    @Autowired
    private ArtistCategoryRepository artistCategoryRepository;

    @Autowired
    private ArtistTrackRepository artistTrackRepository;

    @Test
    @Sql("/test-data/binding-service-test-data.sql")
    void batchUnbind_shouldUnbindExistingArtistBindings_whenValidRequest() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L, 1002L, 1003L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Verify initial state
        assertThat(artistBindingRepository.findBoundArtistsForDataSource(DataSource.LASTFM, externalIds))
            .hasSize(2); // Only 1001 and 1002 exist

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getNotFoundCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactlyInAnyOrder(1001L, 1002L);
        assertThat(response.getNotFound()).containsExactly(1003L);

        // Verify bindings are removed
        assertThat(artistBindingRepository.findBoundArtistsForDataSource(DataSource.LASTFM, externalIds))
            .isEmpty();
    }

    @Test
    @Sql("/test-data/binding-service-test-data.sql")
    void batchUnbind_shouldUnbindExistingCategoryBindings_whenValidRequest() {
        // Given
        List<Long> externalIds = Arrays.asList(2001L, 2002L, 2999L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Verify initial state
        assertThat(categoryBindingRepository.findBoundCategoriesForDataSource(DataSource.LASTFM, externalIds))
            .hasSize(2); // Only 2001 and 2002 exist

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.CATEGORY, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getNotFoundCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactlyInAnyOrder(2001L, 2002L);
        assertThat(response.getNotFound()).containsExactly(2999L);

        // Verify bindings are removed
        assertThat(categoryBindingRepository.findBoundCategoriesForDataSource(DataSource.LASTFM, externalIds))
            .isEmpty();
    }

    @Test
    @Sql("/test-data/binding-service-test-data.sql")
    void batchUnbind_shouldUnbindExistingTrackBindings_whenValidRequest() {
        // Given
        List<Long> externalIds = Arrays.asList(3001L, 3002L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Verify initial state
        assertThat(trackBindingRepository.findBoundTracksForDataSource(DataSource.LASTFM, externalIds))
            .hasSize(2);

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.TRACK, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(2);
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getNotFoundCount()).isEqualTo(0);
        assertThat(response.getSuccessfullyUnbound()).containsExactlyInAnyOrder(3001L, 3002L);
        assertThat(response.getNotFound()).isEmpty();

        // Verify bindings are removed
        assertThat(trackBindingRepository.findBoundTracksForDataSource(DataSource.LASTFM, externalIds))
            .isEmpty();
    }

    @Test
    @Sql("/test-data/binding-service-cascade-test-data.sql")
    void batchUnbind_shouldCascadeDeleteRelationBindings_whenArtistBindingsDeleted() {
        // Given
        List<Long> artistExternalIds = Arrays.asList(1001L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(artistExternalIds)
            .build();

        // Verify initial state - relation bindings exist
        long initialArtistCategoryBindings = entityManager.createQuery(
            "SELECT COUNT(acb) FROM artist_category_binding acb WHERE acb.dataSource = :dataSource AND acb.externalArtistId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 1001L)
            .getSingleResult();

        long initialArtistTrackBindings = entityManager.createQuery(
            "SELECT COUNT(atb) FROM artist_track_binding atb WHERE atb.dataSource = :dataSource AND atb.externalArtistId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 1001L)
            .getSingleResult();

        assertThat(initialArtistCategoryBindings).isGreaterThan(0);
        assertThat(initialArtistTrackBindings).isGreaterThan(0);

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactly(1001L);

        // Verify cascade deletion of relation bindings
        long finalArtistCategoryBindings = entityManager.createQuery(
            "SELECT COUNT(acb) FROM artist_category_binding acb WHERE acb.dataSource = :dataSource AND acb.externalArtistId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 1001L)
            .getSingleResult();

        long finalArtistTrackBindings = entityManager.createQuery(
            "SELECT COUNT(atb) FROM artist_track_binding atb WHERE atb.dataSource = :dataSource AND atb.externalArtistId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 1001L)
            .getSingleResult();

        assertThat(finalArtistCategoryBindings).isEqualTo(0);
        assertThat(finalArtistTrackBindings).isEqualTo(0);
    }

    @Test
    @Sql("/test-data/binding-service-cascade-test-data.sql")
    void batchUnbind_shouldCascadeDeleteRelationBindings_whenCategoryBindingsDeleted() {
        // Given
        List<Long> categoryExternalIds = Arrays.asList(2001L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(categoryExternalIds)
            .build();

        // Verify initial state - relation bindings exist
        long initialArtistCategoryBindings = entityManager.createQuery(
            "SELECT COUNT(acb) FROM artist_category_binding acb WHERE acb.dataSource = :dataSource AND acb.externalCategoryId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 2001L)
            .getSingleResult();

        assertThat(initialArtistCategoryBindings).isGreaterThan(0);

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.CATEGORY, DataSource.LASTFM, request);

        // Then
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactly(2001L);

        // Verify cascade deletion of relation bindings
        long finalArtistCategoryBindings = entityManager.createQuery(
            "SELECT COUNT(acb) FROM artist_category_binding acb WHERE acb.dataSource = :dataSource AND acb.externalCategoryId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 2001L)
            .getSingleResult();

        assertThat(finalArtistCategoryBindings).isEqualTo(0);
    }

    @Test
    @Sql("/test-data/binding-service-cascade-test-data.sql")
    void batchUnbind_shouldCascadeDeleteRelationBindings_whenTrackBindingsDeleted() {
        // Given
        List<Long> trackExternalIds = Arrays.asList(3001L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(trackExternalIds)
            .build();

        // Verify initial state - relation bindings exist
        long initialArtistTrackBindings = entityManager.createQuery(
            "SELECT COUNT(atb) FROM artist_track_binding atb WHERE atb.dataSource = :dataSource AND atb.externalTrackId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 3001L)
            .getSingleResult();

        assertThat(initialArtistTrackBindings).isGreaterThan(0);

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.TRACK, DataSource.LASTFM, request);

        // Then
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactly(3001L);

        // Verify cascade deletion of relation bindings
        long finalArtistTrackBindings = entityManager.createQuery(
            "SELECT COUNT(atb) FROM artist_track_binding atb WHERE atb.dataSource = :dataSource AND atb.externalTrackId = :externalId",
            Long.class)
            .setParameter("dataSource", DataSource.LASTFM)
            .setParameter("externalId", 3001L)
            .getSingleResult();

        assertThat(finalArtistTrackBindings).isEqualTo(0);
    }

    @Test
    void batchUnbind_shouldReturnEmptyResult_whenEmptyRequest() {
        // Given
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(Arrays.asList())
            .build();

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(0);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getNotFoundCount()).isEqualTo(0);
        assertThat(response.getSuccessfullyUnbound()).isEmpty();
        assertThat(response.getNotFound()).isEmpty();
    }

    @Test
    void batchUnbind_shouldReturnNotFoundForAll_whenNoBindingsExist() {
        // Given
        List<Long> nonExistentIds = Arrays.asList(9999L, 9998L, 9997L);
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(nonExistentIds)
            .build();

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getNotFoundCount()).isEqualTo(3);
        assertThat(response.getSuccessfullyUnbound()).isEmpty();
        assertThat(response.getNotFound()).containsExactlyInAnyOrder(9999L, 9998L, 9997L);
    }
}
