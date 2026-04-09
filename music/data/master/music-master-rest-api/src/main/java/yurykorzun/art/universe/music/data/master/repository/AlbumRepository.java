package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.Album;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {

    Optional<Album> findByNameAndPrimaryArtistId(String name, Long primaryArtistId);


    @Query("""
        SELECT a
        FROM album a
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        ORDER BY a.name ASC
    """)
    Page<Album> findAlbums(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT DISTINCT a
        FROM album a
        LEFT JOIN a.categoryRelations ac
        WHERE (:search IS NULL
            OR :search = ''
            OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        AND (:categoryId IS NULL OR ac.categoryId = :categoryId)
        ORDER BY a.name ASC
    """)
    Page<Album> findAlbums(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = """
        SELECT DISTINCT a FROM album a
        LEFT JOIN FETCH a.categoryRelations ac
        LEFT JOIN FETCH ac.category
        WHERE (:search IS NULL OR :search = '' OR LOWER(a.name) LIKE LOWER(CONCAT('%', :search, '%')))
        AND (:categoryId IS NULL OR EXISTS (
            SELECT 1 FROM album_category ac2
            WHERE ac2.albumId = a.id AND ac2.categoryId = :categoryId
        ))
        ORDER BY a.name
        """)
    Page<Album> findAlbumsWithCategories(@Param("search") String search, @Param("categoryId") Long categoryId, Pageable pageable);

    @Query(value = "SELECT a.* FROM album a WHERE LOWER(a.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) ORDER BY a.name ASC LIMIT :limit",
           nativeQuery = true)
    List<Album> findByNameContainingIgnoreCase(@Param("searchTerm") String searchTerm, @Param("limit") int limit);
}
