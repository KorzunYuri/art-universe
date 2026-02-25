package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.ArtistAlbum;

import java.util.List;

@Repository
public interface ArtistAlbumRepository extends JpaRepository<ArtistAlbum, Long> {

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   rt.name AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_album r
            JOIN album t ON r.album_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.artist_id = :artistId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedAlbumsByArtistId(
            @Param("artistId") Long artistId,
            @Param("relationTypeId") Long relationTypeId);

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   COALESCE(rt.reverse_name, rt.name) AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_album r
            JOIN artist t ON r.artist_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.album_id = :albumId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedArtistsByAlbumId(
            @Param("albumId") Long albumId,
            @Param("relationTypeId") Long relationTypeId);
}
