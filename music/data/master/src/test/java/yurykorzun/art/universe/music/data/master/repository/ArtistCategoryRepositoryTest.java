package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import yurykorzun.art.universe.music.data.master.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.Category;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class ArtistCategoryRepositoryTest extends JpaOnlyTest {

    @Autowired
    private ArtistCategoryRepository artistCategoryRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void existsByArtistIdAndCategoryId_shouldReturnTrue_whenRelationExists() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        em.persist(ArtistCategory.builder()
            .artistId(artist.getId())
            .categoryId(category.getId())
            .build());
        em.flush();

        // When
        boolean exists = artistCategoryRepository.existsByArtistIdAndCategoryId(artist.getId(), category.getId());

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByArtistIdAndCategoryId_shouldReturnFalse_whenRelationDoesNotExist() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        em.flush();

        // When
        boolean exists = artistCategoryRepository.existsByArtistIdAndCategoryId(artist.getId(), category.getId());

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void findByArtistIdAndCategoryId_shouldReturnRelation_whenExists() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        ArtistCategory relation = em.persist(ArtistCategory.builder()
            .artistId(artist.getId())
            .categoryId(category.getId())
            .build());
        em.flush();

        // When
        Optional<ArtistCategory> result = artistCategoryRepository.findByArtistIdAndCategoryId(artist.getId(), category.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(relation.getId());
        assertThat(result.get().getArtistId()).isEqualTo(artist.getId());
        assertThat(result.get().getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void findByArtistIdAndCategoryId_shouldReturnEmpty_whenNotExists() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        em.flush();

        // When
        Optional<ArtistCategory> result = artistCategoryRepository.findByArtistIdAndCategoryId(artist.getId(), category.getId());

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void save_shouldCreateRelation() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        em.flush();

        ArtistCategory relation = ArtistCategory.builder()
            .artistId(artist.getId())
            .categoryId(category.getId())
            .build();

        // When
        ArtistCategory saved = artistCategoryRepository.save(relation);
        em.flush();

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getArtistId()).isEqualTo(artist.getId());
        assertThat(saved.getCategoryId()).isEqualTo(category.getId());
    }

    @Test
    void delete_shouldRemoveRelation() {
        // Given
        Artist artist = em.persist(Artist.builder().name("Test Artist").build());
        Category category = em.persist(Category.builder().name("Test Category").build());
        ArtistCategory relation = em.persist(ArtistCategory.builder()
            .artistId(artist.getId())
            .categoryId(category.getId())
            .build());
        em.flush();

        // When
        artistCategoryRepository.delete(relation);
        em.flush();

        // Then
        Optional<ArtistCategory> result = artistCategoryRepository.findByArtistIdAndCategoryId(artist.getId(), category.getId());
        assertThat(result).isEmpty();
    }
}
