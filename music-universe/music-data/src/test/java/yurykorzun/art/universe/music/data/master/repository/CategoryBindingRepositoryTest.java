package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import yurykorzun.art.universe.music.data.master.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
public class CategoryBindingRepositoryTest extends JpaOnlyTest {

    @Autowired
    private CategoryBindingRepository bindingRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldReturnBoundCategoriesWithNames() {
        // given
        Category masterCategory = em.persist(Category.builder().name("Rock").build());
        em.flush();

        CategoryBinding binding = CategoryBinding.builder()
            .masterId(masterCategory.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();
        em.persist(binding);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundCategoriesForDataSource(
            DataSource.LASTFM, List.of(123L)
        );

        // then
        assertThat(results).hasSize(1);
        BoundEntityProjection result = results.get(0);
        assertThat(result.getExternalId()).isEqualTo(123L);
        assertThat(result.getDataSource()).isEqualTo(DataSource.LASTFM);
        assertThat(result.getMasterId()).isEqualTo(masterCategory.getId());
        assertThat(result.getMasterName()).isEqualTo("Rock");
    }

    @Test
    void shouldHandleMultipleBindings() {
        // given
        Category masterCategory1 = em.persist(Category.builder().name("Rock").build());
        Category masterCategory2 = em.persist(Category.builder().name("Jazz").build());
        em.flush();

        CategoryBinding binding1 = CategoryBinding.builder()
            .masterId(masterCategory1.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();

        CategoryBinding binding2 = CategoryBinding.builder()
            .masterId(masterCategory2.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(456L)
            .build();

        em.persist(binding1);
        em.persist(binding2);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundCategoriesForDataSource(
            DataSource.LASTFM, List.of(123L, 456L)
        );

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(BoundEntityProjection::getExternalId)
            .containsExactlyInAnyOrder(123L, 456L);
        assertThat(results).extracting(BoundEntityProjection::getMasterName)
            .containsExactlyInAnyOrder("Rock", "Jazz");
    }

    @Test
    void shouldReturnEmptyListWhenNoMatches() {
        // given
        Category masterCategory = em.persist(Category.builder().name("Rock").build());
        em.flush();

        CategoryBinding binding = CategoryBinding.builder()
            .masterId(masterCategory.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();
        em.persist(binding);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundCategoriesForDataSource(
            DataSource.LASTFM, List.of(456L)
        );

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleDifferentDataSources() {
        // given
        Category masterCategory = em.persist(Category.builder().name("Rock").build());
        em.flush();

        CategoryBinding lastfmBinding = CategoryBinding.builder()
            .masterId(masterCategory.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();

        CategoryBinding spotifyBinding = CategoryBinding.builder()
            .masterId(masterCategory.getId())
            .dataSource(DataSource.SPOTIFY)
            .externalId(456L)
            .build();

        em.persist(lastfmBinding);
        em.persist(spotifyBinding);
        em.flush();

        // when
        List<BoundEntityProjection> lastfmResults = bindingRepository.findBoundCategoriesForDataSource(
            DataSource.LASTFM, List.of(123L)
        );

        List<BoundEntityProjection> spotifyResults = bindingRepository.findBoundCategoriesForDataSource(
            DataSource.SPOTIFY, List.of(456L)
        );

        // then
        assertThat(lastfmResults).hasSize(1);
        assertThat(spotifyResults).hasSize(1);
        assertThat(lastfmResults.get(0).getExternalId()).isEqualTo(123L);
        assertThat(spotifyResults.get(0).getExternalId()).isEqualTo(456L);
    }

    @Test
    void shouldReturnSingleBoundCategory() {
        // given
        Category masterCategory = em.persist(Category.builder().name("Electronic").build());
        em.flush();

        CategoryBinding binding = CategoryBinding.builder()
            .masterId(masterCategory.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(789L)
            .build();
        em.persist(binding);
        em.flush();

        // when
        BoundEntityProjection result = bindingRepository.findBoundCategoryForDataSource(
            DataSource.LASTFM, 789L
        );

        // then
        assertThat(result).isNotNull();
        assertThat(result.getExternalId()).isEqualTo(789L);
        assertThat(result.getDataSource()).isEqualTo(DataSource.LASTFM);
        assertThat(result.getMasterId()).isEqualTo(masterCategory.getId());
        assertThat(result.getMasterName()).isEqualTo("Electronic");
    }

    @Test
    void shouldReturnNullWhenSingleCategoryNotFound() {
        // when
        BoundEntityProjection result = bindingRepository.findBoundCategoryForDataSource(
            DataSource.LASTFM, 999L
        );

        // then
        assertThat(result).isNull();
    }
}
