package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.TrackBinding;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrackBindingRepository extends JpaRepository<TrackBinding, Long> {

    /**
     * Returns bindings for a list of tracks from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external track ids
     * @return list of bindings for tracks that are already bound
     */
    @Query("""
        SELECT  tb.externalId       AS externalId,
                tb.dataSource       AS dataSource,
                tb.masterId         AS masterId,
                t.name              AS masterName,
                t.primaryArtistId   AS masterPrimaryArtistId
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
    
    /**
     * Find a binding by data source and external ID
     * 
     * @param dataSource The data source
     * @param externalId The external ID
     * @return The binding if found
     */
    Optional<TrackBinding> findByDataSourceAndExternalId(DataSource dataSource, Long externalId);

}
