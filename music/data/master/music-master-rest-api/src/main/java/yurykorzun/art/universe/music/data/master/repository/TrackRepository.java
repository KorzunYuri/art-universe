package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.Track;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {

    Optional<Track> findByNameAndPrimaryArtistId(String name, Long primaryArtistId);

    @Query("""
        SELECT t
        FROM track t
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY t.name ASC
    """)
    Page<Track> findTracks(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT DISTINCT t
        FROM track t
        LEFT JOIN t.categoryRelations tc
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:categoryId IS NULL OR tc.categoryId = :categoryId)
        ORDER BY t.name ASC
    """)
    Page<Track> findTracks(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT t FROM track t
        LEFT JOIN FETCH t.categoryRelations tc
        LEFT JOIN FETCH tc.category
        WHERE (:search IS NULL OR :search = '' OR LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR EXISTS (
            SELECT 1 FROM track_category tc2
            WHERE tc2.trackId = t.id AND tc2.categoryId = :categoryId
        ))
        ORDER BY t.name
        """)
    Page<Track> findTracksWithCategories(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT t.* FROM track t WHERE LOWER(t.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY t.name ASC LIMIT :limit",
           nativeQuery = true)
    List<Track> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("limit") int limit);
}
