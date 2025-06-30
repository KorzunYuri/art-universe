package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBindingRepository extends JpaRepository<CategoryBinding, Long> {

    /**
     * Returns bindings for a list of categories from external source.
     * @param dataSource    data source code
     * @param externalIds   list of external category ids
     * @return list of bindings for categories that are already bound
     */
    @Query("""
        SELECT  cb.externalId   AS externalId,
                cb.dataSource   AS dataSource,
                cb.referenceId  AS referenceId,
                c.name          AS referenceName
        FROM
            category_binding cb
        JOIN
            cb.category c
        WHERE   cb.dataSource = :dataSource
            AND cb.externalId IN :externalIds
    """)
    List<BoundEntityProjection> findBoundCategoriesForDataSource(
        @Param("dataSource")    DataSource dataSource,
        @Param("externalIds")   List<Long> externalIds
    );
    
    /**
     * Returns binding for a single category from external source.
     * @param dataSource    data source code
     * @param externalId    external category id
     * @return binding for category if it exists, null otherwise
     */
    @Query("""
        SELECT  cb.externalId   AS externalId,
                cb.dataSource   AS dataSource,
                cb.referenceId  AS referenceId,
                c.name          AS referenceName
        FROM
            category_binding cb
        JOIN
            cb.category c
        WHERE   cb.dataSource = :dataSource
            AND cb.externalId = :externalId
    """)
    BoundEntityProjection findBoundCategoryForDataSource(
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
    Optional<CategoryBinding> findByDataSourceAndExternalId(DataSource dataSource, Long externalId);

}
