package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

/**
 * Implementation for dimension lookup operations that allows empty search
 */
public class DimensionLookupService extends BaseLookupService {

    public DimensionLookupService(EntityManager entityManager, EntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected boolean isValidSearchRequest(LookupRequestDTO request) {
        // For dimensions, request is always valid (even with null or empty search)
        return true;
    }

    @Override
    protected SqlQueryBuilder.QueryData buildQuery(EntityMetadata metadata, LookupRequestDTO request, int limit) {
        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder();
        if (request.getSearch() == null || request.getSearch().trim().isEmpty()) {
            return
                new SqlQueryBuilder()
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