package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.Artist;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByName(String name);
    
    /**
     * Search artists with pagination
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param pageable Pagination and sorting parameters
     * @return Page of artists
     */
    @Query("""
        SELECT a
        FROM artist a
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY a.name ASC
    """)
    Page<Artist> findArtists(@Param("search") String search, Pageable pageable);

    /**
     * Search artists with pagination and optional category filter
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param categoryId Optional category ID to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of artists
     */
    @Query("""
        SELECT DISTINCT a
        FROM artist a
        LEFT JOIN a.categoryRelations ac
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:categoryId IS NULL OR ac.categoryId = :categoryId)
        ORDER BY a.name ASC
    """)
    Page<Artist> findArtists(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * Find artists with their categories
     * 
     * @param search Optional search term (case insensitive, partial match)
     * @param categoryId Optional category ID to filter by
     * @param pageable Pagination and sorting parameters
     * @return Page of artists with categories loaded
     */
    @Query(value = """
        SELECT DISTINCT a FROM artist a
        LEFT JOIN FETCH a.categoryRelations ac
        LEFT JOIN FETCH ac.category
        WHERE (:search IS NULL OR :search = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR EXISTS (
            SELECT 1 FROM artist_category ac2 
            WHERE ac2.artistId = a.id AND ac2.categoryId = :categoryId
        ))
        ORDER BY a.name
        """)
    Page<Artist> findArtistsWithCategories(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);
    
    /**
     * Find artists by name containing the search term (case insensitive)
     * with sorting by name in ascending order and limiting results
     * 
     * @param searchTerm The search term to look for in artist names
     * @param limit Maximum number of results to return
     * @return List of artists matching the search term, sorted by name
     */
    @Query(value = "SELECT a.* FROM artist a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.name ASC LIMIT :limit", 
           nativeQuery = true)
    List<Artist> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("limit") int limit);
}
