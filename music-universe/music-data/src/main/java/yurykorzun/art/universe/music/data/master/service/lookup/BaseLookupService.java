package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for standard entity lookup operations
 */
public class BaseLookupService extends AbstractLookupService<LookupRequestDTO> {

    public BaseLookupService(EntityManager entityManager, EntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected SqlQueryBuilder.QueryData buildQuery(EntityMetadata metadata, LookupRequestDTO request, int limit) {
        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder();
        sqlQueryBuilder.append(String.format(
            """
            SELECT e.id, e.name FROM %s e
            WHERE LOWER(e.name) LIKE LOWER(CONCAT('%%', ?1, '%%'))
            ORDER BY e.name ASC
            LIMIT ?2
            """,
            metadata.getTableName()
        ));

        sqlQueryBuilder.param(1, request.getSearch().trim());
        sqlQueryBuilder.param(2, limit);

        return sqlQueryBuilder.build();
    }

    @Override
    protected List<LookupResultDTO> mapResultsToDto(List<Object[]> results) {
        return results.stream()
            .map(row -> LookupResultDTO.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .build())
            .collect(Collectors.toList());
    }

    @Override
    protected LookupRequestDTO prepareRequest(LookupRequestDTO request, int defaultLimit) {
        return LookupRequestDTO.builder()
            .search(request.getSearch())
            .limit(request.getLimit() != null ? request.getLimit() : defaultLimit)
            .build();
    }
}