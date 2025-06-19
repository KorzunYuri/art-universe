package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.TrackBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

@Repository
public interface TrackBindingRepository extends JpaRepository<TrackBinding, Long> {

    /**
     * Returns bindings for a list of tracks from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external track ids
     * @return list of bindings for tracks that are already bound
     */
    @Query("""
        SELECT  tb.externalId   AS externalId,
                tb.dataSource   AS dataSource,
                tb.referenceId  AS referenceId,
                t.name          AS referenceName
        FROM
            track_binding tb
        JOIN
            tb.track t
        WHERE   tb.dataSource = :dataSource
            AND tb.externalId IN :externalIds
    """)
    List<BoundEntityProjection> findBoundTracksForDataSource(
        @Param("dataSource")    DataSource dataSource,
        @Param("externalIds")   List<Long> externalIds
    );

}
