package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, Long> {

    List<AlbumTrack> findByAlbumId(Long albumId);

    Optional<AlbumTrack> findByAlbumIdAndTrackIdAndRelationTypeId(Long albumId, Long trackId, Long relationTypeId);

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   rt.name AS relationTypeName,
                   r.track_order AS trackOrder
            FROM album_track r
            JOIN track t ON r.track_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.album_id = :albumId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            ORDER BY r.track_order
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedTracksByAlbumId(
            @Param("albumId") Long albumId,
            @Param("relationTypeId") Long relationTypeId);

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   COALESCE(rt.reverse_name, rt.name) AS relationTypeName,
                   r.track_order AS trackOrder
            FROM album_track r
            JOIN album t ON r.album_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.track_id = :trackId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedAlbumsByTrackId(
            @Param("trackId") Long trackId,
            @Param("relationTypeId") Long relationTypeId);
}
