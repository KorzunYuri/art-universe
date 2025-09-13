package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.CategoryCountsProjection;
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
     * Search categories with pagination
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of categories
     */
    @Query("SELECT c FROM category c WHERE (:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Category> findCategories(@Param("search") String search, Pageable pageable);

    /**
     * Find categories with their parent information
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @return List of categories with parent information
     */
    @Query(value = """
        SELECT c FROM category c
        LEFT JOIN FETCH c.parentRelations pr
        LEFT JOIN FETCH pr.sourceCategory
        WHERE (:search IS NULL OR :search = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')))
        ORDER BY c.name
        """)
    List<Category> findCategoriesWithParentsEntities(@Param("search") String search);

    /**
     * Find all categories with hierarchy counts (children, artists, tracks)
     * 
     * @return List of categories with calculated counts
     */
    @Query(value = """
        WITH RECURSIVE category_descendants AS (
            SELECT id as category_id, id as descendant_id, 0 as depth
            FROM category
            UNION ALL
            SELECT cd.category_id, cc.target_category_id as descendant_id, cd.depth + 1
            FROM category_descendants cd
            JOIN category_category cc ON cd.descendant_id = cc.source_category_id
            WHERE cd.depth < 10
        ),
        distinct_descendants AS (
            SELECT DISTINCT category_id, descendant_id
            FROM category_descendants
        ),
        children_counts AS (
            SELECT category_id, COUNT(DISTINCT descendant_id) - 1 as children_count
            FROM distinct_descendants GROUP BY category_id
        ),
        artists_counts AS (
            SELECT dd.category_id, COUNT(DISTINCT ac.artist_id) as artists_count
            FROM distinct_descendants dd
            LEFT JOIN artist_category ac ON dd.descendant_id = ac.category_id
            GROUP BY dd.category_id
        ),
        tracks_counts AS (
            SELECT dd.category_id, COUNT(DISTINCT at.track_id) as tracks_count
            FROM distinct_descendants dd
            LEFT JOIN artist_category ac ON dd.descendant_id = ac.category_id
            LEFT JOIN artist_track at ON ac.artist_id = at.artist_id
            GROUP BY dd.category_id
        )
        SELECT c.id, c.name,
               COALESCE(cc.children_count, 0) as children_count,
               COALESCE(ac.artists_count, 0) as artists_count,
               COALESCE(tc.tracks_count, 0) as tracks_count
        FROM category c
        LEFT JOIN children_counts cc ON c.id = cc.category_id
        LEFT JOIN artists_counts ac ON c.id = ac.category_id
        LEFT JOIN tracks_counts tc ON c.id = tc.category_id
        """, nativeQuery = true)
    List<CategoryCountsProjection> findCategoriesWithCounts();
}
