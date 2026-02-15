package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.AlbumBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumBindingRepository extends JpaRepository<AlbumBinding, Long> {

    Optional<AlbumBinding> findByDataSourceAndExternalId(DataSource dataSource, Long externalId);


    /**
     * Returns bindings for a list of albums from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external album ids
     * @return list of bindings for albums that are already bound
     */
    @Query("""
        SELECT  ab.externalId   AS externalId,
                ab.dataSource   AS dataSource,
                ab.masterId     AS masterId,
                a.name          AS masterName
        FROM
            album_binding ab
        JOIN
            ab.album a
        WHERE   ab.dataSource = :dataSource
            AND ab.externalId IN :externalIds
    """)
    List<BoundEntityProjection> findBoundAlbumsForDataSource(
        @Param("dataSource")    DataSource dataSource,
        @Param("externalIds")   List<Long> externalIds
    );

}
