package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Universal service implementation for entity binding operations
 */
@Service
public class BindingServiceImpl implements BindingService {
    
    private final EntityManager entityManager;
    
    public BindingServiceImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    
    @Override
    @Transactional
    public BatchUnbindResponseDTO batchUnbind(EntityType entityType, DataSource dataSource, BatchUnbindRequestDTO request) {
        List<Long> externalIds = request.getExternalIds();
        
        if (externalIds == null || externalIds.isEmpty()) {
            return BatchUnbindResponseDTO.builder()
                .successfullyUnbound(Collections.emptyList())
                .notFound(Collections.emptyList())
                .totalProcessed(0)
                .successCount(0)
                .notFoundCount(0)
                .build();
        }
        
        EntityMetadata metadata = new EntityMetadata(entityType);
        
        // Find existing bindings
        List<Long> existingIds = findExistingExternalIds(metadata, dataSource, externalIds);
        
        // Delete existing bindings
        int deletedCount = deleteByDataSourceAndExternalIdIn(metadata, dataSource, existingIds);
        
        // Determine non-existing IDs
        List<Long> notFoundIds = externalIds.stream()
            .filter(id -> !existingIds.contains(id))
            .collect(Collectors.toList());
        
        return BatchUnbindResponseDTO.builder()
            .successfullyUnbound(existingIds)
            .notFound(notFoundIds)
            .totalProcessed(externalIds.size())
            .successCount(existingIds.size())
            .notFoundCount(notFoundIds.size())
            .build();
    }
    
    /**
     * Find existing external IDs that have bindings in the database
     * 
     * @param metadata Entity metadata for table names
     * @param dataSource Data source to filter by
     * @param externalIds List of external IDs to check
     * @return List of external IDs that exist in bindings table
     */
    private List<Long> findExistingExternalIds(EntityMetadata metadata, DataSource dataSource, List<Long> externalIds) {
        if (externalIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Create placeholders for IN clause
        String placeholders = externalIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(","));
        
        String sql = """
            SELECT external_id 
            FROM %s 
            WHERE data_source_id = ? AND external_id IN (%s)
            """.formatted(metadata.getBindingTableName(), placeholders);
        
        try {
            Query query = entityManager.createNativeQuery(sql)
                .setParameter(1, dataSource.getCode());
            
            // Set parameters for IN clause
            for (int i = 0; i < externalIds.size(); i++) {
                query.setParameter(i + 2, externalIds.get(i));
            }
            
            List<Object> results = query.getResultList();
            return results.stream()
                .map(result -> ((Number) result).longValue())
                .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to find existing external IDs", e);
        }
    }
    
    /**
     * Delete bindings by data source and external IDs
     * 
     * @param metadata Entity metadata for table names
     * @param dataSource Data source to filter by
     * @param externalIds List of external IDs to delete
     * @return Number of deleted records
     */
    private int deleteByDataSourceAndExternalIdIn(EntityMetadata metadata, DataSource dataSource, List<Long> externalIds) {
        if (externalIds.isEmpty()) {
            return 0;
        }
        
        // Create placeholders for IN clause
        String placeholders = externalIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(","));
        
        String sql = """
            DELETE FROM %s 
            WHERE data_source_id = ? AND external_id IN (%s)
            """.formatted(metadata.getBindingTableName(), placeholders);
        
        try {
            Query query = entityManager.createNativeQuery(sql)
                .setParameter(1, dataSource.getCode());
            
            // Set parameters for IN clause
            for (int i = 0; i < externalIds.size(); i++) {
                query.setParameter(i + 2, externalIds.get(i));
            }
            
            return query.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete bindings", e);
        }
    }
}
