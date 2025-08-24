package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup;

import jakarta.persistence.EntityManager;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.service.lookup.BaseLookupService;
import yurykorzun.art.universe.common.service.lookup.SqlQueryBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityMetadata;

/**
 * Common lookup service for LastFM entities with approval status prioritization.
 * Provides lookup functionality with custom ordering:
 * 1. Name match priority (exact > starts with > contains)
 * 2. Approval status priority (APPROVED > PRE_APPROVED > PENDING > DECLINED)
 * 3. Alphabetical by name
 */
public class LastfmEntityLookupService extends BaseLookupService<LastfmEntityType, LastfmEntityMetadata> {

    public LastfmEntityLookupService(EntityManager entityManager, LastfmEntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected LastfmEntityMetadata createEntityMetadata() {
        return new LastfmEntityMetadata(getEntityType());
    }

    @Override
    protected SqlQueryBuilder.QueryData buildQuery(LastfmEntityMetadata metadata, LookupRequestDTO request, int limit) {
        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder();
        sqlQueryBuilder.append(String.format("""
                SELECT e.id, e.name
                FROM %s e
                WHERE LOWER(e.name) LIKE LOWER(CONCAT('%%', ?1, '%%'))
                ORDER BY
                    CASE
                        WHEN ?1 = '' THEN 2
                        WHEN LOWER(e.name) = LOWER(?1) THEN 0
                        WHEN LOWER(e.name) LIKE LOWER(CONCAT(?1, '%%')) THEN 1
                        ELSE 2
                    END,
                    CASE e.approval_status
                        WHEN 2 THEN 0  -- APPROVED
                        WHEN 4 THEN 1  -- PRE_APPROVED
                        WHEN 1 THEN 2  -- PENDING
                        WHEN 3 THEN 3  -- DECLINED
                        ELSE 4
                    END,
                    e.name ASC
                LIMIT ?2
            """,
            metadata.getTableName()
        ));

        sqlQueryBuilder.param(1, request.getSearch().trim());
        sqlQueryBuilder.param(2, limit);

        return sqlQueryBuilder.build();
    }
}
