package yurykorzun.art.universe.music.data.master.repository;

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
