package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.TrackCategory;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackCategoryRepository extends JpaRepository<TrackCategory, Long> {

    boolean existsByTrackIdAndCategoryId(Long trackId, Long categoryId);

    Optional<TrackCategory> findByTrackIdAndCategoryId(Long trackId, Long categoryId);

    @Query(value = """
            SELECT c.id AS id, c.name AS name, r.id AS relationId,
                   NULL AS relationTypeId, NULL AS relationTypeName, NULL AS trackOrder
            FROM track_category r
            JOIN category c ON r.category_id = c.id
            WHERE r.track_id = :trackId
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedCategoriesByTrackId(@Param("trackId") Long trackId);
}
