package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.service.lookup.SqlQueryBuilder;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Implementation for dimension lookup operations that allows empty search
 */
public class DimensionLookupService extends MasterEntityLookupService {

    public DimensionLookupService(EntityManager entityManager) {
        super(entityManager, MasterEntityType.DIMENSION);
    }

    @Override
    protected boolean isValidSearchRequest(LookupRequestDTO request) {
        // For dimensions, request is always valid (even with null or empty search)
        return true;
    }

    @Override
    protected SqlQueryBuilder.QueryData buildQuery(MasterEntityMetadata metadata, LookupRequestDTO request, int limit) {
        if (request.getSearch() == null || request.getSearch().trim().isEmpty()) {
            return new SqlQueryBuilder()
                .append(String.format("""
                        SELECT e.id, e.name
                        FROM %s e
                        ORDER BY e.name ASC
                        LIMIT ?1
                    """,
                    metadata.getTableName()))
                .param(1, limit)
                .build();
        }
        return super.buildQuery(metadata, request, limit);
    }
}
