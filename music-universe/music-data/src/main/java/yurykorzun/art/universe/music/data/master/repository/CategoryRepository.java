package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
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
}
