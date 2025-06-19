package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.AlbumBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

@Repository
public interface AlbumBindingRepository extends JpaRepository<AlbumBinding, Long> {

    /**
     * Returns bindings for a list of albums from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external album ids
     * @return list of bindings for albums that are already bound
     */
    @Query("""
        SELECT  ab.externalId   AS externalId,
                ab.dataSource   AS dataSource,
                ab.referenceId  AS referenceId,
                a.name          AS referenceName
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
