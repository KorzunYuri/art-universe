package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtistBindingRepository extends JpaRepository<ArtistBinding, Long> {

    /**
     * Returns bindings for a list of artists from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external artists ids
     * @return list of bindings for artists that are already bound
     */
    @Query("""
        SELECT  ab.externalId   AS externalId,
                ab.dataSource   AS dataSource,
                ab.referenceId  AS referenceId,
                a.name          AS referenceName
        FROM
            artist_binding ab
        JOIN
            ab.artist a
        WHERE   ab.dataSource = :dataSource
            AND ab.externalId IN :externalIds
    """)
    List<BoundEntityProjection> findBoundArtistsForDataSource(
        @Param("dataSource")    DataSource dataSource,
        @Param("externalIds")   List<Long> externalIds
    );
    
    /**
     * Returns binding for a single artist from external source.
     * @param dataSource    data source code
     * @param externalId    external artist id
     * @return binding for artist if it exists, null otherwise
     */
    @Query("""
        SELECT  ab.externalId   AS externalId,
                ab.dataSource   AS dataSource,
                ab.referenceId  AS referenceId,
                a.name          AS referenceName
        FROM
            artist_binding ab
        JOIN
            ab.artist a
        WHERE   ab.dataSource = :dataSource
            AND ab.externalId = :externalId
    """)
    BoundEntityProjection findBoundArtistForDataSource(
        @Param("dataSource")    DataSource dataSource,
        @Param("externalId")    Long externalId
    );
    
    /**
     * Find a binding by data source and external ID
     * 
     * @param dataSource The data source
     * @param externalId The external ID
     * @return The binding if found
     */
    Optional<ArtistBinding> findByDataSourceAndExternalId(DataSource dataSource, Long externalId);

}
