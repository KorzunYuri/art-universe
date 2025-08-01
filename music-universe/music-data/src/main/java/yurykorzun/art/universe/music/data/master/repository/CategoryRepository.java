package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.entity.Category;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its exact name
     * 
     * @param name The exact name of the category
     * @return The category if found
     */
    Optional<Category> findByName(String name);

    /**
     * Search categories with hierarchy information and pagination
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of categories with hierarchy information
     */
    @Query(nativeQuery = true, 
            countQuery = """
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
                   category_hierarchy ch
                       ON c.parent_id = ch.id
                )
                SELECT COUNT(*)
                FROM
                    category_hierarchy ch
                LEFT JOIN
                    dimension d
                        ON ch.dimension_id = d.id
                LEFT JOIN
                    dimension ed
                        ON ch.effective_dimension_id = ed.id
                LEFT JOIN
                    category parent
                        ON ch.parent_id = parent.id
                WHERE
                (
                :search IS NULL OR :search = ''
                OR   LOWER(ch.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                OR   LOWER(parent.name)  LIKE LOWER(CONCAT('%', :search, '%'))
                OR   LOWER(d.name)       LIKE LOWER(CONCAT('%', :search, '%'))
                OR   LOWER(ed.name)      LIKE LOWER(CONCAT('%', :search, '%')))
                """,
           value = """
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
                   d.name AS dimensionName,
                   ed.name AS effectiveDimensionName,
                   parent.name AS parentName
               FROM
                   category_hierarchy ch
               LEFT JOIN
                   dimension d ON ch.dimension_id = d.id
               LEFT JOIN
                   dimension ed ON ch.effective_dimension_id = ed.id
               LEFT JOIN
                   category parent ON ch.parent_id = parent.id
               WHERE
                   (    :search IS NULL
                    OR  :search = ''
                    OR  LOWER(ch.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                    OR  LOWER(parent.name)  LIKE LOWER(CONCAT('%', :search, '%'))
                    OR  LOWER(d.name)       LIKE LOWER(CONCAT('%', :search, '%'))
                    OR  LOWER(ed.name)      LIKE LOWER(CONCAT('%', :search, '%'))
                    )
               """)
    Page<CategoryHierarchyProjection> searchCategories(@Param("search") String search, Pageable pageable);

    /**
     * Find a single category with hierarchy information by ID
     * 
     * @param id The category ID
     * @return Category with hierarchy information if found
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
            d.name AS dimensionName,
            ed.name AS effectiveDimensionName,
            parent.name AS parentName
        FROM
            category_hierarchy ch
        LEFT JOIN
            dimension d ON ch.dimension_id = d.id
        LEFT JOIN
            dimension ed ON ch.effective_dimension_id = ed.id
        LEFT JOIN
            category parent ON ch.parent_id = parent.id
        WHERE
            ch.id = :id
    """)
    Optional<CategoryHierarchyProjection> findByIdWithHierarchy(@Param("id") Long id);
    
    /**
     * Find categories by name containing the search term (case insensitive)
     * with sorting by name in ascending order and limiting results
     * 
     * @param searchTerm The search term to look for in category names
     * @param limit Maximum number of results to return
     * @return List of categories matching the search term, sorted by name
     */
    @Query(value = "SELECT c.* FROM category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY c.name ASC LIMIT :limit", 
           nativeQuery = true)
    List<Category> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("limit") int limit);
}
