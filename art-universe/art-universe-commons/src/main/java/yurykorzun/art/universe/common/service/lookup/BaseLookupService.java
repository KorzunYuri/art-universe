package yurykorzun.art.universe.common.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.common.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.common.persistence.entity.BaseEntityMetadata;
import yurykorzun.art.universe.common.persistence.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parametrized base implementation for entity lookup operations
 * @param <T> The entity type
 * @param <M> The metadata type for the entity
 */
@Slf4j
public abstract class BaseLookupService<T extends EntityType, M extends BaseEntityMetadata<T>> {

    protected final EntityManager entityManager;
    private final T entityType;

    public BaseLookupService(EntityManager entityManager, T entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
    }

    /**
     * Performs lookup for a single search request
     */
    public List<LookupResultDTO> lookup(LookupRequestDTO request) {
        if (!isValidSearchRequest(request)) {
            return List.of();
        }

        int limit = request.getLimit() != null ? request.getLimit() : 20;
        
        // Validate limit
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        // Create metadata and build query
        M metadata = createEntityMetadata();
        SqlQueryBuilder.QueryData queryData = buildQuery(metadata, request, limit);

        Query query = entityManager.createNativeQuery(queryData.getSql());
        queryData.getParametersSetter().accept(query);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapResultsToDto(results);
    }

    /**
     * Performs batch lookup for multiple search requests
     */
    public BatchLookupResponseDTO batchLookup(BaseBatchLookupRequestDTO request) {
        if (request.getSearchRequests() == null || request.getSearchRequests().isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Apply default batch limit if null
        int defaultBatchLimit = request.getLimit() != null ? request.getLimit() : 20;

        // Filter and prepare search requests
        List<LookupRequestDTO> validRequests = filterAndPrepareRequests(request.getSearchRequests(), defaultBatchLimit);

        if (validRequests.isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Execute individual lookups and collect results
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        
        for (LookupRequestDTO req : validRequests) {
            String searchTerm = req.getSearch() != null ? req.getSearch() : "";
            List<LookupResultDTO> results = lookup(req);
            resultMap.put(searchTerm, results);
        }

        // Build response
        return BatchLookupResponseDTO.builder()
            .results(resultMap)
            .build();
    }

    /**
     * Creates entity metadata - must be implemented by subclasses
     */
    protected abstract M createEntityMetadata();

    /**
     * Validates if the search request is valid - can be overridden in subclasses
     */
    protected boolean isValidSearchRequest(LookupRequestDTO request) {
        boolean isValid = request.getSearch() != null && !request.getSearch().trim().isEmpty();
        if (!isValid) {
            log.error("Invalid search request: {}", request.getSearch());
        }
        return isValid;
    }

    /**
     * Builds SQL query - can be overridden in subclasses for custom queries
     */
    protected SqlQueryBuilder.QueryData buildQuery(M metadata, LookupRequestDTO request, int limit) {
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
                    e.name ASC
                LIMIT ?2
            """,
            metadata.getTableName()
        ));

        sqlQueryBuilder.param(1, request.getSearch().trim());
        sqlQueryBuilder.param(2, limit);

        return sqlQueryBuilder.build();
    }

    /**
     * Maps database results to DTOs - can be overridden in subclasses
     */
    protected List<LookupResultDTO> mapResultsToDto(List<Object[]> results) {
        return results.stream()
            .map(row -> LookupResultDTO.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Filters and prepares requests for batch processing
     */
    protected List<LookupRequestDTO> filterAndPrepareRequests(List<LookupRequestDTO> requests, int defaultLimit) {
        return requests.stream()
            .filter(this::isValidSearchRequest)
            .map(req -> prepareRequest(req, defaultLimit))
            .collect(Collectors.toList());
    }

    /**
     * Prepares a single request for processing
     */
    protected LookupRequestDTO prepareRequest(LookupRequestDTO request, int defaultLimit) {
        return LookupRequestDTO.builder()
            .search(request.getSearch())
            .limit(request.getLimit() != null ? request.getLimit() : defaultLimit)
            .build();
    }

    /**
     * Gets the entity type this service works with
     */
    public T getEntityType() {
        return entityType;
    }
}
