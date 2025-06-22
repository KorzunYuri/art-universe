package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.approved.entity.Category;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Finds all categories with their hierarchy information.
     * For each category, it determines:
     * - The effective dimension ID (inherited from ancestors if not set directly)
     * - The hierarchy level (depth in the category tree)
     *
     * @return List of categories with hierarchy information
     */
    @Query(nativeQuery = true, value = """
        WITH RECURSIVE category_hierarchy AS (
            -- Base case: root categories (no parent)
            SELECT
                c.id,
                c.name,
                c.dimension_id,
                c.dimension_id AS effective_dimension_id,
                c.parent_id,
                1 AS hierarchy_level
            FROM
                category c
            WHERE
                c.parent_id IS NULL
                
            UNION ALL
            
            -- Recursive case: child categories
            SELECT
                c.id,
                c.name,
                c.dimension_id,
                COALESCE(c.dimension_id, ch.effective_dimension_id) AS effective_dimension_id,
                c.parent_id,
                ch.hierarchy_level + 1 AS hierarchy_level
            FROM
                category c
            JOIN
                category_hierarchy ch ON c.parent_id = ch.id
        )
        SELECT
            ch.id AS id,
            ch.name AS name,
            ch.dimension_id AS dimensionId,
            ch.effective_dimension_id AS effectiveDimensionId,
            ch.parent_id AS parentId,
            ch.hierarchy_level AS hierarchyLevel,
            d.name AS dimensionName
        FROM
            category_hierarchy ch
        LEFT JOIN
            dimension d ON ch.effective_dimension_id = d.id
        ORDER BY
            ch.hierarchy_level, ch.name
    """)
    List<CategoryHierarchyProjection> findAllWithHierarchy();
}
