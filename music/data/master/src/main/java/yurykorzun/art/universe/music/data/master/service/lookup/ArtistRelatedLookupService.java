package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.domain.service.lookup.SqlQueryBuilder;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.ArtistRelatedLookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementation for artist-related entity lookup operations
 */
@Slf4j
public class ArtistRelatedLookupService extends MasterEntityLookupService {

    public ArtistRelatedLookupService(EntityManager entityManager, MasterEntityType entityType) {
        super(entityManager, entityType);
    }

    /**
     * Performs lookup for artist-related entities
     */
    public List<LookupResultDTO> lookup(ArtistRelatedLookupRequestDTO request) {
        if (!isValidSearchRequest(request)) {
            return List.of();
        }

        int limit = request.getLimit() != null ? request.getLimit() : 20;
        
        // Validate limit
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        // Create metadata and build query
        MasterEntityMetadata metadata = createEntityMetadata();
        SqlQueryBuilder.QueryData queryData = buildArtistRelatedQuery(metadata, request, limit);

        var query = entityManager.createNativeQuery(queryData.getSql());
        queryData.getParametersSetter().accept(query);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapArtistRelatedResultsToDto(results);
    }

    /**
     * Performs batch lookup for multiple artist-related search requests
     */
    public BatchLookupResponseDTO batchLookup(ArtistRelatedBatchLookupRequestDTO request) {
        if (request.getSearchRequests() == null || request.getSearchRequests().isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Apply default batch limit if null
        int defaultBatchLimit = request.getLimit() != null ? request.getLimit() : 20;

        // Filter and prepare search requests
        List<ArtistRelatedLookupRequestDTO> validRequests = filterAndPrepareArtistRequests(request.getSearchRequests(), defaultBatchLimit);

        if (validRequests.isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Execute individual lookups and collect results
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        
        for (ArtistRelatedLookupRequestDTO req : validRequests) {
            String searchTerm = req.getSearch() != null ? req.getSearch() : "";
            List<LookupResultDTO> results = lookup(req);
            resultMap.put(searchTerm, results);
        }

        // Build response
        return BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
    }

    protected boolean isValidSearchRequest(ArtistRelatedLookupRequestDTO request) {
        // empty name is applicable if artist is provided
        boolean isValid =
            (request.getSearch() != null && !request.getSearch().trim().isEmpty())
            || (request.getMasterArtistId() != null)
            || (request.getDataSource() != null && request.getExternalArtistId() != null);
        if (!isValid) {
            log.error("Invalid search request: {}, masterArtistId: {}, dataSource: {}, externalArtistId: {}",
                request.getSearch(), request.getMasterArtistId(), request.getDataSource(), request.getExternalArtistId());
        }
        return isValid;
    }

    protected SqlQueryBuilder.QueryData buildArtistRelatedQuery(MasterEntityMetadata metadata, ArtistRelatedLookupRequestDTO request, int limit) {
        SqlQueryBuilder sqlQueryBuilder = new SqlQueryBuilder();
        sqlQueryBuilder.append(String.format(
            """
            SELECT
                e.id,
                e.name,
                a.id    AS artist_id,
                a.name  AS artist_name
            FROM
                %s e
            JOIN
                artist a
                    ON e.primary_artist_id = a.id
            WHERE
                    (
                            ?1 IS NULL
                        OR  ?1 = ''
                        OR  LOWER(e.name) LIKE LOWER(CONCAT('%%', ?1, '%%'))
                    )
                AND
                    (
                        (
                            CAST(?2 as BIGINT) IS NOT NULL 
                            AND e.primary_artist_id = CAST(?2 as BIGINT)
                        )
                        OR
                        (
                            CAST(?2 as BIGINT) IS NULL
                            AND CAST(?3 as BIGINT) IS NOT NULL
                            AND CAST(?4 as BIGINT) IS NOT NULL
                            AND EXISTS (
                                SELECT 1
                                FROM artist_binding ab
                                WHERE   ab.data_source_id   = CAST(?3 as BIGINT)
                                    AND ab.external_id      = CAST(?4 as BIGINT)
                                    AND ab.master_id        = e.primary_artist_id
                                )
                        )
                        OR
                        (
                            CAST(?2 as BIGINT) IS NULL
                            AND CAST(?3 as BIGINT) IS NULL
                            AND CAST(?4 as BIGINT) IS NULL
                        )
                    )
            ORDER BY
                CASE
                    WHEN    CAST(?2 as BIGINT) IS NOT NULL
                        AND e.primary_artist_id = CAST(?2 as BIGINT)    THEN 0
                                                                        ELSE 1
                END,
                e.name ASC
            LIMIT ?5
            """,
            metadata.getTableName()
        ));

        // set parameters
        sqlQueryBuilder.param(1, request.getSearch() != null ? request.getSearch().trim() : "");
        sqlQueryBuilder.param(2, request.getMasterArtistId());

        // Set data source and external artist ID parameters
        if (request.getExternalArtistId() != null) {
            sqlQueryBuilder.param(3, request.getDataSource().getCode());
            sqlQueryBuilder.param(4, request.getExternalArtistId());
        } else {
            sqlQueryBuilder.param(3, null);
            sqlQueryBuilder.param(4, null);
        }

        sqlQueryBuilder.param(5, limit);

        return sqlQueryBuilder.build();
    }

    protected List<LookupResultDTO> mapArtistRelatedResultsToDto(List<Object[]> results) {
        return results.stream()
            .map(row -> {
                Long id = ((Number) row[0]).longValue();
                String name = (String) row[1];
                String artistName = (String) row[3];

                return LookupResultDTO.builder()
                    .id(id)
                    .name(artistName + " - " + name)
                    .build();
            })
            .collect(Collectors.toList());
    }

    /**
     * Filters and prepares artist-related requests for batch processing
     */
    protected List<ArtistRelatedLookupRequestDTO> filterAndPrepareArtistRequests(List<ArtistRelatedLookupRequestDTO> requests, int defaultLimit) {
        return requests.stream()
            .filter(this::isValidSearchRequest)
            .map(req -> prepareArtistRequest(req, defaultLimit))
            .collect(Collectors.toList());
    }

    /**
     * Prepares a single artist-related request for processing
     */
    protected ArtistRelatedLookupRequestDTO prepareArtistRequest(ArtistRelatedLookupRequestDTO request, int defaultLimit) {
        return ArtistRelatedLookupRequestDTO.builder()
            .search(request.getSearch())
            .dataSource(request.getDataSource())
            .masterArtistId(request.getMasterArtistId())
            .externalArtistId(request.getExternalArtistId())
            .limit(request.getLimit() != null ? request.getLimit() : defaultLimit)
            .build();
    }
}
