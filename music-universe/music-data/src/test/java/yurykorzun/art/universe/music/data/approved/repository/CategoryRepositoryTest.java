package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.entity.Dimension;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class CategoryRepositoryTest extends JpaOnlyTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private DimensionRepository dimensionRepository;

    @Autowired
    private TestEntityManager em;

    private Dimension dimension1;
    private Dimension dimension2;
    private Category categoryA;
    private Category categoryAA;
    private Category categoryAAA;
    private Category categoryAAAA;
    private Category categoryB;

    @BeforeEach
    void setUp() {
        // Create dimensions
        dimension1 = Dimension.builder().name("Genre").build();
        dimension2 = Dimension.builder().name("Mood").build();
        dimensionRepository.save(dimension1);
        dimensionRepository.save(dimension2);
        em.flush();

        // Create category hierarchy
        // A (Genre) -> AA (null) -> AAA (Mood) -> AAAA (null)
        // B (Genre)
        categoryA = Category.builder()
            .name("Rock")
            .dimensionId(dimension1.getId())
            .build();
        categoryRepository.save(categoryA);
        em.flush();

        categoryAA = Category.builder()
            .name("Alternative Rock")
            .parentId(categoryA.getId())
            // No dimension set, should inherit from parent
            .build();
        categoryRepository.save(categoryAA);
        em.flush();

        categoryAAA = Category.builder()
            .name("Melancholic Alternative")
            .parentId(categoryAA.getId())
            .dimensionId(dimension2.getId())
            .build();
        categoryRepository.save(categoryAAA);
        em.flush();

        categoryAAAA = Category.builder()
            .name("Sad Alternative")
            .parentId(categoryAAA.getId())
            // No dimension set, should inherit from parent
            .build();
        categoryRepository.save(categoryAAAA);
        em.flush();

        categoryB = Category.builder()
            .name("Jazz")
            .dimensionId(dimension1.getId())
            .build();
        categoryRepository.save(categoryB);
        em.flush();
    }

    @Test
    void searchCategories_withNoSearch_shouldReturnAllCategories() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void searchCategories_withEmptySearch_shouldReturnAllCategories() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories("", PageRequest.of(0, 10));

        // Then
        assertThat(result.getContent()).hasSize(5);
        assertThat(result.getTotalElements()).isEqualTo(5);
    }

    @Test
    void searchCategories_byName_shouldReturnMatchingCategories() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories("rock", PageRequest.of(0, 10));

        // Then - should find "Rock", "Alternative Rock", and "Melancholic Alternative" (child of Alternative Rock)
        assertThat(result.getContent()).hasSize(3);
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        assertThat(names).containsExactlyInAnyOrder("Rock", "Alternative Rock", "Melancholic Alternative");
    }

    @Test
    void searchCategories_byParentName_shouldReturnMatchingCategories() {
        // When - search for categories that have "Rock" as parent
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories("rock", PageRequest.of(0, 10));

        // Then - should find "Rock" itself, "Alternative Rock" (child of Rock), and "Melancholic Alternative" (child of Alternative Rock)
        assertThat(result.getContent()).hasSize(3);
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        assertThat(names).containsExactlyInAnyOrder("Rock", "Alternative Rock", "Melancholic Alternative");
    }

    @Test
    void searchCategories_byDimensionName_shouldReturnMatchingCategories() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories("genre", PageRequest.of(0, 10));

        // Then - should find categories with Genre dimension (direct) and effective dimension (inherited)
        assertThat(result.getContent()).hasSize(3);
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        assertThat(names).containsExactlyInAnyOrder("Rock", "Jazz", "Alternative Rock");
    }

    @Test
    void searchCategories_byEffectiveDimensionName_shouldReturnMatchingCategories() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories("mood", PageRequest.of(0, 10));

        // Then - should find categories with Mood as effective dimension
        assertThat(result.getContent()).hasSize(2);
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        assertThat(names).containsExactlyInAnyOrder("Melancholic Alternative", "Sad Alternative");
    }

    @Test
    void searchCategories_withPagination_shouldReturnCorrectPage() {
        // When
        Page<CategoryHierarchyProjection> firstPage = categoryRepository.searchCategories(null, PageRequest.of(0, 2));
        Page<CategoryHierarchyProjection> secondPage = categoryRepository.searchCategories(null, PageRequest.of(1, 2));

        // Then
        assertThat(firstPage.getContent()).hasSize(2);
        assertThat(secondPage.getContent()).hasSize(2);
        assertThat(firstPage.getTotalElements()).isEqualTo(5);
        assertThat(secondPage.getTotalElements()).isEqualTo(5);
        assertThat(firstPage.getTotalPages()).isEqualTo(3);
    }

    @Test
    void searchCategories_sortedByName_shouldReturnSortedResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").ascending());
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, pageable);

        // Then
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        
        assertThat(names).containsExactly(
            "Alternative Rock", "Jazz", "Melancholic Alternative", "Rock", "Sad Alternative"
        );
    }

    @Test
    void searchCategories_sortedByNameDescending_shouldReturnSortedResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("name").descending());
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, pageable);

        // Then
        List<String> names = result.getContent().stream()
            .map(CategoryHierarchyProjection::getName)
            .collect(Collectors.toList());
        
        assertThat(names).containsExactly(
            "Sad Alternative", "Rock", "Melancholic Alternative", "Jazz", "Alternative Rock"
        );
    }

    @Test
    void searchCategories_sortedByDimensionName_shouldReturnSortedResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("dimensionName").ascending().and(Sort.by("name").ascending()));
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, pageable);

        // Then
        List<CategoryHierarchyProjection> content = result.getContent();
        
        // Categories should be sorted by dimensionName (null values first in PostgreSQL), then by name
        // Expected order: Genre (Jazz, Rock), Mood (Melancholic Alternative), null (Alternative Rock, Sad Alternative)
        assertThat(content.get(0).getDimensionName()).isEqualTo("Genre"); // Jazz
        assertThat(content.get(0).getName()).isEqualTo("Jazz");
        assertThat(content.get(1).getDimensionName()).isEqualTo("Genre"); // Rock
        assertThat(content.get(1).getName()).isEqualTo("Rock");
        assertThat(content.get(2).getDimensionName()).isEqualTo("Mood"); // Melancholic Alternative
        assertThat(content.get(2).getName()).isEqualTo("Melancholic Alternative");
        assertThat(content.get(3).getDimensionName()).isNull(); // Alternative Rock
        assertThat(content.get(3).getName()).isEqualTo("Alternative Rock");
        assertThat(content.get(4).getDimensionName()).isNull(); // Sad Alternative
        assertThat(content.get(4).getName()).isEqualTo("Sad Alternative");
    }

    @Test
    void searchCategories_sortedByEffectiveDimensionName_shouldReturnSortedResults() {
        // When
        Pageable pageable = PageRequest.of(0, 10, Sort.by("effectiveDimensionName").ascending().and(Sort.by("name").ascending()));
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, pageable);

        // Then
        List<CategoryHierarchyProjection> content = result.getContent();
        
        // All should have effective dimension names (Genre or Mood)
        assertThat(content.get(0).getEffectiveDimensionName()).isEqualTo("Genre"); // Alternative Rock
        assertThat(content.get(1).getEffectiveDimensionName()).isEqualTo("Genre"); // Jazz
        assertThat(content.get(2).getEffectiveDimensionName()).isEqualTo("Genre"); // Rock
        assertThat(content.get(3).getEffectiveDimensionName()).isEqualTo("Mood"); // Melancholic Alternative
        assertThat(content.get(4).getEffectiveDimensionName()).isEqualTo("Mood"); // Sad Alternative
    }

    @Test
    void searchCategories_shouldReturnCorrectHierarchyInformation() {
        // When
        Page<CategoryHierarchyProjection> result = categoryRepository.searchCategories(null, PageRequest.of(0, 10));

        // Then
        Map<String, CategoryHierarchyProjection> categoryMap = result.getContent().stream()
            .collect(Collectors.toMap(CategoryHierarchyProjection::getName, Function.identity()));

        // Check Rock
        CategoryHierarchyProjection rock = categoryMap.get("Rock");
        assertThat(rock.getDimensionId()).isEqualTo(dimension1.getId());
        assertThat(rock.getEffectiveDimensionId()).isEqualTo(dimension1.getId());
        assertThat(rock.getParentId()).isNull();
        assertThat(rock.getHierarchyLevel()).isEqualTo(1);
        assertThat(rock.getDimensionName()).isEqualTo("Genre");
        assertThat(rock.getEffectiveDimensionName()).isEqualTo("Genre");

        // Check Alternative Rock
        CategoryHierarchyProjection altRock = categoryMap.get("Alternative Rock");
        assertThat(altRock.getDimensionId()).isNull();
        assertThat(altRock.getEffectiveDimensionId()).isEqualTo(dimension1.getId()); // Inherited from Rock
        assertThat(altRock.getParentId()).isEqualTo(categoryA.getId());
        assertThat(altRock.getHierarchyLevel()).isEqualTo(2);
        assertThat(altRock.getDimensionName()).isNull(); // No direct dimension
        assertThat(altRock.getEffectiveDimensionName()).isEqualTo("Genre"); // Inherited

        // Check Melancholic Alternative
        CategoryHierarchyProjection melancholic = categoryMap.get("Melancholic Alternative");
        assertThat(melancholic.getDimensionId()).isEqualTo(dimension2.getId());
        assertThat(melancholic.getEffectiveDimensionId()).isEqualTo(dimension2.getId());
        assertThat(melancholic.getParentId()).isEqualTo(categoryAA.getId());
        assertThat(melancholic.getHierarchyLevel()).isEqualTo(3);
        assertThat(melancholic.getDimensionName()).isEqualTo("Mood");
        assertThat(melancholic.getEffectiveDimensionName()).isEqualTo("Mood");

        // Check Sad Alternative
        CategoryHierarchyProjection sad = categoryMap.get("Sad Alternative");
        assertThat(sad.getDimensionId()).isNull();
        assertThat(sad.getEffectiveDimensionId()).isEqualTo(dimension2.getId()); // Inherited from Melancholic Alternative
        assertThat(sad.getParentId()).isEqualTo(categoryAAA.getId());
        assertThat(sad.getHierarchyLevel()).isEqualTo(4);
        assertThat(sad.getDimensionName()).isNull(); // No direct dimension
        assertThat(sad.getEffectiveDimensionName()).isEqualTo("Mood"); // Inherited
    }

    @Test
    void findByIdWithHierarchy_shouldReturnCategoryWithHierarchyInfo() {
        // When
        var result = categoryRepository.findByIdWithHierarchy(categoryAA.getId());

        // Then
        assertThat(result).isPresent();
        CategoryHierarchyProjection altRock = result.get();
        assertThat(altRock.getName()).isEqualTo("Alternative Rock");
        assertThat(altRock.getDimensionId()).isNull();
        assertThat(altRock.getEffectiveDimensionId()).isEqualTo(dimension1.getId());
        assertThat(altRock.getParentId()).isEqualTo(categoryA.getId());
        assertThat(altRock.getHierarchyLevel()).isEqualTo(2);
        assertThat(altRock.getDimensionName()).isNull();
        assertThat(altRock.getEffectiveDimensionName()).isEqualTo("Genre");
    }

    @Test
    void findByIdWithHierarchy_whenNotFound_shouldReturnEmpty() {
        // When
        var result = categoryRepository.findByIdWithHierarchy(999L);

        // Then
        assertThat(result).isEmpty();
    }
}
