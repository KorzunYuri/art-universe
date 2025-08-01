package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base service for entity lookup operations
 * @param <T> Type of lookup request
 */
@Slf4j
public abstract class AbstractLookupService<T extends LookupRequestDTO> {

    protected final EntityManager entityManager;
    protected final EntityType entityType;

    protected AbstractLookupService(EntityManager entityManager, EntityType entityType) {
        this.entityManager = entityManager;
        this.entityType = entityType;
    }

    /**
     * Performs lookup for a single search request
     */
    public List<LookupResultDTO> lookup(T request) {
        if (!isValidSearchRequest(request)) {
            return List.of();
        }

        int limit = request.getLimit() != null ? request.getLimit() : 20;
        
        // Validate limit
        if (limit <= 0) {
            throw new IllegalArgumentException("Limit must be greater than zero");
        }

        // Create SQL query based on EntityMetadata
        EntityMetadata metadata = new EntityMetadata(entityType);
        SqlQueryBuilder.QueryData queryData = buildQuery(metadata, request, limit);

        Query query = entityManager.createNativeQuery(queryData.getSql());
        queryData.getParametersSetter().accept(query);

        @SuppressWarnings("unchecked")
        List<Object[]> results = query.getResultList();
        return mapResultsToDto(results);
    }

    /**
     * Performs batch lookup for multiple search requests by calling lookup() for each request
     */
    public BatchLookupResponseDTO batchLookup(BatchLookupRequestDTO<T> request) {
        if (request.getSearchRequests() == null || request.getSearchRequests().isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Apply default batch limit if null
        int defaultBatchLimit = request.getLimit() != null ? request.getLimit() : 20;

        // Filter and prepare search requests
        List<T> validRequests = filterAndPrepareRequests(request.getSearchRequests(), defaultBatchLimit);

        if (validRequests.isEmpty()) {
            return BatchLookupResponseDTO.builder().build();
        }

        // Execute individual lookups and collect results
        Map<String, List<LookupResultDTO>> resultMap = new HashMap<>();
        
        for (T req : validRequests) {
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
     * Validates if the search request is valid
     */
    protected boolean isValidSearchRequest(T request) {
        boolean isValid = request.getSearch() != null && !request.getSearch().trim().isEmpty();
        if (!isValid) {
            log.error("Invalid search request: {}", request.getSearch());
        }
        return isValid;
    }

    /**
     * Builds SQL query and parameter setters for a single lookup request
     */
    protected abstract SqlQueryBuilder.QueryData buildQuery(EntityMetadata metadata, T request, int limit);

    /**
     * Maps database results to DTOs
     */
    protected abstract List<LookupResultDTO> mapResultsToDto(List<Object[]> results);

    /**
     * Filters and prepares requests for batch processing
     */
    protected List<T> filterAndPrepareRequests(List<T> requests, int defaultLimit) {
        return requests.stream()
            .filter(this::isValidSearchRequest)
            .map(req -> prepareRequest(req, defaultLimit))
            .collect(Collectors.toList());
    }

    /**
     * Prepares a single request for processing
     */
    protected abstract T prepareRequest(T request, int defaultLimit);
}
