package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.ArtistArtist;

import java.util.List;

@Repository
public interface ArtistArtistRepository extends JpaRepository<ArtistArtist, Long> {

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   rt.name AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_artist r
            JOIN artist t ON r.target_artist_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.source_artist_id = :artistId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            UNION ALL
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   COALESCE(rt.reverse_name, rt.name) AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_artist r
            JOIN artist t ON r.source_artist_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.target_artist_id = :artistId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedArtists(
            @Param("artistId") Long artistId,
            @Param("relationTypeId") Long relationTypeId);
}
