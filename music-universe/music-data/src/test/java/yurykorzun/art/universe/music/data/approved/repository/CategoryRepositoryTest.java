package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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

    @BeforeEach
    void setUp() {
        // Create dimensions
        dimension1 = Dimension.builder().name("Dimension 1").build();
        dimension2 = Dimension.builder().name("Dimension 2").build();
        dimensionRepository.save(dimension1);
        dimensionRepository.save(dimension2);
        em.flush();

        // Create category hierarchy
        // A (dimension1) -> AA (null) -> AAA (dimension2) -> AAAA (null)
        categoryA = Category.builder()
            .name("Category A")
            .dimensionId(dimension1.getId())
            .build();
        categoryRepository.save(categoryA);
        em.flush();

        categoryAA = Category.builder()
            .name("Category AA")
            .parentId(categoryA.getId())
            // No dimension set, should inherit from parent
            .build();
        categoryRepository.save(categoryAA);
        em.flush();

        categoryAAA = Category.builder()
            .name("Category AAA")
            .parentId(categoryAA.getId())
            .dimensionId(dimension2.getId())
            .build();
        categoryRepository.save(categoryAAA);
        em.flush();

        categoryAAAA = Category.builder()
            .name("Category AAAA")
            .parentId(categoryAAA.getId())
            // No dimension set, should inherit from parent
            .build();
        categoryRepository.save(categoryAAAA);
        em.flush();
    }

    @Test
    void findAllWithHierarchy_shouldReturnCorrectHierarchyInformation() {
        // When
        List<CategoryHierarchyProjection> result = categoryRepository.findAllWithHierarchy();

        // Then
        assertThat(result).hasSize(4);

        // Convert to map for easier assertions
        Map<String, CategoryHierarchyProjection> categoryMap = result.stream()
            .collect(Collectors.toMap(CategoryHierarchyProjection::getName, Function.identity()));

        // Check Category A
        CategoryHierarchyProjection catA = categoryMap.get("Category A");
        assertThat(catA.getDimensionId()).isEqualTo(dimension1.getId());
        assertThat(catA.getEffectiveDimensionId()).isEqualTo(dimension1.getId());
        assertThat(catA.getParentId()).isNull();
        assertThat(catA.getHierarchyLevel()).isEqualTo(1);
        assertThat(catA.getDimensionName()).isEqualTo("Dimension 1");

        // Check Category AA
        CategoryHierarchyProjection catAA = categoryMap.get("Category AA");
        assertThat(catAA.getDimensionId()).isNull();
        assertThat(catAA.getEffectiveDimensionId()).isEqualTo(dimension1.getId()); // Inherited from A
        assertThat(catAA.getParentId()).isEqualTo(categoryA.getId());
        assertThat(catAA.getHierarchyLevel()).isEqualTo(2);
        assertThat(catAA.getDimensionName()).isEqualTo("Dimension 1");

        // Check Category AAA
        CategoryHierarchyProjection catAAA = categoryMap.get("Category AAA");
        assertThat(catAAA.getDimensionId()).isEqualTo(dimension2.getId());
        assertThat(catAAA.getEffectiveDimensionId()).isEqualTo(dimension2.getId());
        assertThat(catAAA.getParentId()).isEqualTo(categoryAA.getId());
        assertThat(catAAA.getHierarchyLevel()).isEqualTo(3);
        assertThat(catAAA.getDimensionName()).isEqualTo("Dimension 2");

        // Check Category AAAA
        CategoryHierarchyProjection catAAAA = categoryMap.get("Category AAAA");
        assertThat(catAAAA.getDimensionId()).isNull();
        assertThat(catAAAA.getEffectiveDimensionId()).isEqualTo(dimension2.getId()); // Inherited from AAA
        assertThat(catAAAA.getParentId()).isEqualTo(categoryAAA.getId());
        assertThat(catAAAA.getHierarchyLevel()).isEqualTo(4);
        assertThat(catAAAA.getDimensionName()).isEqualTo("Dimension 2");
    }
}
