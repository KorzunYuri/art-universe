package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistCategoryRepository extends JpaRepository<ArtistCategory, Long> {

    boolean existsByArtistIdAndCategoryId(Long artistId, Long categoryId);

    Optional<ArtistCategory> findByArtistIdAndCategoryId(Long artistId, Long categoryId);

    @Query(value = """
            SELECT c.id AS id, c.name AS name, r.id AS relationId,
                   NULL AS relationTypeId, NULL AS relationTypeName, NULL AS trackOrder
            FROM artist_category r
            JOIN category c ON r.category_id = c.id
            WHERE r.artist_id = :artistId
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedCategoriesByArtistId(@Param("artistId") Long artistId);
}
