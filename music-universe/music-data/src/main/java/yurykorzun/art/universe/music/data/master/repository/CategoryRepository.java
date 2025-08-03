package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.entity.Category;

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
                SELECT COUNT(*)
                FROM
                    mv_category_hierarchy ch
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
                   mv_category_hierarchy ch
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
    Page<CategoryHierarchyProjection> findCategories(@Param("search") String search, Pageable pageable);

    /**
     * Find a single category with hierarchy information by ID
     * 
     * @param id The category ID
     * @return Category with hierarchy information if found
     */
    @Query(nativeQuery = true, value = """
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
            mv_category_hierarchy ch
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
}
