package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.entity.Dimension;

import java.util.List;
import java.util.Optional;

@Repository
public interface DimensionRepository extends JpaRepository<Dimension, Long> {

    /**
     * Search dimensions with pagination
     * 
     * @param query Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of dimensions matching the search criteria
     */
    @Query(value = """
        SELECT d FROM dimension d
        WHERE (:query IS NULL OR :query = '' OR LOWER(d.name) LIKE LOWER(CONCAT('%', :query, '%')))
        """)
    Page<Dimension> searchDimensions(@Param("query") String query, Pageable pageable);
    
    /**
     * Find dimensions by name containing the search term (case insensitive)
     * with sorting by name in ascending order and limiting results
     * 
     * @param searchTerm The search term to look for in dimension names
     * @param limit Maximum number of results to return
     * @return List of dimensions matching the search term, sorted by name
     */
    @Query(value = "SELECT d.* FROM dimension d WHERE LOWER(d.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY d.name ASC LIMIT :limit", 
           nativeQuery = true)
    List<Dimension> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("limit") int limit);
    
    /**
     * Find all dimensions with sorting by name in ascending order and limiting results
     * 
     * @param limit Maximum number of results to return
     * @return List of all dimensions, sorted by name
     */
    @Query(value = "SELECT d.* FROM dimension d ORDER BY d.name ASC LIMIT :limit", 
           nativeQuery = true)
    List<Dimension> findAllWithLimit(@Param("limit") int limit);
    
    /**
     * Find dimension by exact name (case insensitive)
     * 
     * @param name The dimension name to search for
     * @return Optional containing the dimension if found
     */
    Optional<Dimension> findByNameIgnoreCase(String name);
}
