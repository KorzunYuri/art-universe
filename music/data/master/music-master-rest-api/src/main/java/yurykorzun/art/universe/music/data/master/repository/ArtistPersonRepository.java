package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.ArtistPerson;

import java.util.List;

@Repository
public interface ArtistPersonRepository extends JpaRepository<ArtistPerson, Long> {

    @Query(value = """
            SELECT p.id AS id, p.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   rt.name AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_person r
            JOIN art_view.v_person p ON r.person_id = p.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.artist_id = :artistId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedPersonsByArtistId(
            @Param("artistId") Long artistId,
            @Param("relationTypeId") Long relationTypeId);

    @Query(value = """
            SELECT t.id AS id, t.name AS name, r.id AS relationId,
                   r.relation_type_id AS relationTypeId,
                   COALESCE(rt.reverse_name, rt.name) AS relationTypeName,
                   NULL AS trackOrder
            FROM artist_person r
            JOIN artist t ON r.artist_id = t.id
            LEFT JOIN relation_type rt ON r.relation_type_id = rt.id
            WHERE r.person_id = :personId
              AND (CAST(:relationTypeId AS BIGINT) IS NULL OR r.relation_type_id = :relationTypeId)
            """, nativeQuery = true)
    List<RelatedEntityProjection> findRelatedArtistsByPersonId(
            @Param("personId") Long personId,
            @Param("relationTypeId") Long relationTypeId);
}
