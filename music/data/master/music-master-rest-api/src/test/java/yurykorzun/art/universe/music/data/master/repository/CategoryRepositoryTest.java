package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryCategory;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class CategoryRepositoryTest extends BaseMasterDataJpaTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CategoryCategoryRepository categoryCategoryRepository;

    @Autowired
    private TestEntityManager em;

    private Category categoryMusic;
    private Category categoryGenre;
    private Category categoryRock;
    private Category categoryAlternative;
    private Category categoryJazz;

    @BeforeEach
    void setUp() {
        // Create simple category hierarchy: Music -> Genre -> Rock -> Alternative
        //                                          -> Jazz
        categoryMusic = Category.builder().name("Music").build();
        categoryRepository.save(categoryMusic);
        em.flush();

        categoryGenre = Category.builder().name("Genre").build();
        categoryRepository.save(categoryGenre);
        em.flush();

        categoryRock = Category.builder().name("Rock").build();
        categoryRepository.save(categoryRock);
        em.flush();

        categoryAlternative = Category.builder().name("Alternative").build();
        categoryRepository.save(categoryAlternative);
        em.flush();

        categoryJazz = Category.builder().name("Jazz").build();
        categoryRepository.save(categoryJazz);
        em.flush();

        // Create relationships: Music -> Genre -> Rock -> Alternative
        //                              -> Jazz
        CategoryCategory musicToGenre = CategoryCategory.builder()
            .sourceCategoryId(categoryMusic.getId())
            .targetCategoryId(categoryGenre.getId())
            .build();
        categoryCategoryRepository.save(musicToGenre);

        CategoryCategory genreToRock = CategoryCategory.builder()
            .sourceCategoryId(categoryGenre.getId())
            .targetCategoryId(categoryRock.getId())
            .build();
        categoryCategoryRepository.save(genreToRock);

        CategoryCategory rockToAlternative = CategoryCategory.builder()
            .sourceCategoryId(categoryRock.getId())
            .targetCategoryId(categoryAlternative.getId())
            .build();
        categoryCategoryRepository.save(rockToAlternative);

        CategoryCategory genreToJazz = CategoryCategory.builder()
            .sourceCategoryId(categoryGenre.getId())
            .targetCategoryId(categoryJazz.getId())
            .build();
        categoryCategoryRepository.save(genreToJazz);

        em.flush();
        em.clear(); // Clear persistence context to force reload from database
    }

    @Test
    void findCategories_withNoSearch_shouldReturnAllCategories() {
        // When
        Page<Category> result = categoryRepository.findCategories(null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void findCategories_withEmptySearch_shouldReturnAllCategories() {
        // When
        Page<Category> result = categoryRepository.findCategories("", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void findCategories_byName_shouldReturnMatchingCategories() {
        // When
        Page<Category> result = categoryRepository.findCategories("rock", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Rock");
    }

    @Test
    void findCategories_withPagination_shouldReturnCorrectPage() {
        // When
        Page<Category> firstPage = categoryRepository.findCategories(null, PageRequest.of(0, 2));
        Page<Category> secondPage = categoryRepository.findCategories(null, PageRequest.of(1, 2));

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(secondPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
    }

    @Test
    void findCategories_sortedByName_shouldReturnSortedResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<Category> result = categoryRepository.findCategories(null, pageable);

        // Then
        List<String> names = result.getContent().stream()
            .map(Category::getName)
            .collect(Collectors.toList());
        
        assertThat(names).containsExactly(
            "Alternative", "Genre", "Jazz", "Music", "Rock"
        );
    }

    @Test
    void findCategoriesWithParentsEntities_shouldReturnCategoriesWithParentRelations() {
        // When
        Page<Category> result = categoryRepository.findCategoriesWithParentsEntities("rock", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(1);
        Category rock = result.getContent().getFirst();
        assertThat(rock.getName()).isEqualTo("Rock");
        
        // Check parent relations are loaded
        assertThat(rock.getParentRelations()).hasSize(1);
        assertThat(rock.getParentRelations().getFirst().getSourceCategory().getName()).isEqualTo("Genre");
    }

    @Test
    void findCategoriesWithParentsEntities_withNoSearch_shouldReturnAllCategories() {
        // When
        Page<Category> result = categoryRepository.findCategoriesWithParentsEntities(null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(5);
        
        // Check that categories with parents have relations loaded
        Category alternative = result.getContent().stream()
            .filter(c -> c.getName().equals("Alternative"))
            .findFirst()
            .orElseThrow();
        assertThat(alternative.getParentRelations()).hasSize(1);
        assertThat(alternative.getParentRelations().getFirst().getSourceCategory().getName()).isEqualTo("Rock");
    }
}
