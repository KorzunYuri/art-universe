package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.EntityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.relation.RelationBindingEntity;
import yurykorzun.art.universe.music.data.master.relation.RelationEntity;
import yurykorzun.art.universe.music.data.master.relation.RelationRegistry;
import yurykorzun.art.universe.music.data.master.relation.metadata.EntityMetadata;
import yurykorzun.art.universe.music.data.master.relation.metadata.RelationMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of the service for working with entity relations
 */
@Service
public class RelationServiceImpl implements RelationService {

    private final EntityManager entityManager;
    private final RelationRegistry relationRegistry;

    public RelationServiceImpl(EntityManager entityManager, RelationRegistry relationRegistry) {
        this.entityManager = entityManager;
        this.relationRegistry = relationRegistry;
    }

    @Override
    @Transactional
    public RelationBindingDTO bindExternalRelation(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        Long targetExternalEntityId
    ) {
        // Check that external entities are bound
        Long sourceInternalId = findInternalEntityId(dataSource, sourceEntityType, sourceExternalEntityId);
        Long targetInternalId = findInternalEntityId(dataSource, targetEntityType, targetExternalEntityId);
        
        if (sourceInternalId == null) {
            throw new EntityNotFoundException(
                String.format("External %s with ID %d is not bound", sourceEntityType.getName(), sourceExternalEntityId));
        }
        
        if (targetInternalId == null) {
            throw new EntityNotFoundException(
                String.format("External %s with ID %d is not bound", targetEntityType.getName(), targetExternalEntityId));
        }
        
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Find or create relation
        Long relationId = findOrCreateRelation(
            metadata,
            sourceInternalId,
            targetInternalId
        );
        
        // Find existing binding or create a new one
        Optional<? extends RelationBindingEntity> existingBinding = findExistingBinding(
            metadata, dataSource, sourceExternalEntityId, targetExternalEntityId);
        
        if (existingBinding.isPresent()) {
            // Update binding if it already exists
            RelationBindingEntity binding = existingBinding.get();
            binding.setMasterBindingId(relationId);
            entityManager.merge(binding);
        } else {
            // Create a new binding
            createBinding(
                metadata, dataSource, relationId, 
                sourceExternalEntityId, targetExternalEntityId);
        }
        
        // Get entity names
        String sourceEntityName = getEntityName(sourceEntityType, sourceInternalId);
        String targetEntityName = getEntityName(targetEntityType, targetInternalId);
        
        // Create DTO
        return RelationBindingDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .targetExternalId(targetExternalEntityId)
            .dataSource(dataSource)
            .relationId(relationId)
            .sourceEntityName(sourceEntityName)
            .targetEntityName(targetEntityName)
            .sourceEntityType(sourceEntityType)
            .targetEntityType(targetEntityType)
            .build();
    }

    @Override
    @Transactional
    public boolean unbindExternalRelation(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        Long targetExternalEntityId
    ) {
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Find existing binding
        Optional<? extends RelationBindingEntity> existingBinding = findExistingBinding(
            metadata, dataSource, sourceExternalEntityId, targetExternalEntityId);
        
        if (existingBinding.isPresent()) {
            // Remove binding
            entityManager.remove(existingBinding.get());
            return true;
        }
        
        return false;
    }
    
    @Override
    @Transactional(readOnly = true)
    public RelationBindingStatusDTO findBoundExternalRelations(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        List<Long> targetExternalEntityIds
    ) {
        // 1. Get information about source entity with name in a single query
        Map<Long, EntityInfo> sourceEntityInfo = findEntityInfoByExternalIds(
            dataSource, sourceEntityType, Collections.singletonList(sourceExternalEntityId));
        
        EntityInfo sourceInfo = sourceEntityInfo.getOrDefault(sourceExternalEntityId, 
            new EntityInfo(null, "Unknown", false));
        
        // 2. Create base result structure
        RelationBindingStatusDTO result = RelationBindingStatusDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .sourceEntityType(sourceEntityType)
            .sourceEntityName(sourceInfo.name)
            .sourceInternalId(sourceInfo.internalId)
            .sourceEntityBound(sourceInfo.isBound)
            .targetEntityType(targetEntityType)
            .targetBindings(new ArrayList<>())
            .build();
        
        // If source entity is not bound or no target entities provided, relations cannot exist
        if (!sourceInfo.isBound || targetExternalEntityIds == null || targetExternalEntityIds.isEmpty()) {
            return result;
        }
        
        // 3. Get information about target entities in a single query
        Map<Long, EntityInfo> targetEntitiesInfo = findEntityInfoByExternalIds(
            dataSource, targetEntityType, targetExternalEntityIds);
        
        // 4. Find relations between source and target entities in a single query
        Map<Long, Long> relationMap = findRelations(
            sourceEntityType, sourceInfo.internalId, 
            targetEntityType, targetEntitiesInfo.values().stream()
                .filter(info -> info.isBound)
                .map(info -> info.internalId)
                .collect(Collectors.toList())
        );
        
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // 5. Build result
        for (Long targetExternalId : targetExternalEntityIds) {
            EntityInfo targetInfo = targetEntitiesInfo.getOrDefault(targetExternalId, 
                new EntityInfo(null, "Unknown", false));
            
            boolean isInternalRelationBound = false;
            boolean isExternalRelationBound = false;
            Long relationId = null;
            
            if (targetInfo.isBound) {
                relationId = relationMap.get(targetInfo.internalId);
                isInternalRelationBound = relationId != null;
                
                // Check if external relation is bound
                if (isInternalRelationBound) {
                    Optional<? extends RelationBindingEntity> existingBinding = findExistingBinding(
                        metadata, dataSource, sourceExternalEntityId, targetExternalId);
                    isExternalRelationBound = existingBinding.isPresent();
                }
            }
            
            result.getTargetBindings().add(TargetEntityBindingDTO.builder()
                .targetExternalId(targetExternalId)
                .targetEntityName(targetInfo.name)
                .targetInternalId(targetInfo.internalId)
                .targetEntityBound(targetInfo.isBound)
                .internalRelationBound(isInternalRelationBound)
                .externalRelationBound(isExternalRelationBound)
                .internalRelationId(relationId)
                .build());
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityDTO> getRelatedEntities(
        EntityType sourceEntityType, 
        Long sourceEntityId, 
        EntityType targetEntityType
    ) {
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Form SQL query
        String sql = """
            SELECT t.id, t.name 
            FROM %s r 
            JOIN %s t ON r.%s = t.id 
            WHERE r.%s = ?1
            """.formatted(
                metadata.getRelationTableName(),
                targetEntityType.getName(),
                metadata.getTargetIdField(),
                metadata.getSourceIdField()
            );
        
        // Execute query
        List<Object[]> results;
        try {
            results = entityManager.createNativeQuery(sql)
                .setParameter(1, sourceEntityId)
                .getResultList();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get related entities", e);
        }
        
        // Convert results to DTOs
        List<EntityDTO> entities = new ArrayList<>();
        for (Object[] row : results) {
            entities.add(EntityDTO.builder()
                .id(((Number) row[0]).longValue())
                .name((String) row[1])
                .entityType(targetEntityType)
                .build());
        }
        
        return entities;
    }
    
    @Override
    @Transactional
    public Long createInternalRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId
    ) {
        // Validate that entities exist
        validateEntityExists(sourceEntityType, sourceEntityId);
        validateEntityExists(targetEntityType, targetEntityId);
        
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Create or find the relation
        return findOrCreateRelation(metadata, sourceEntityId, targetEntityId);
    }
    
    @Override
    @Transactional
    public boolean deleteInternalRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId
    ) {
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Form SQL query to find the relation
        String findSql = """
            SELECT id 
            FROM %s 
            WHERE %s = ?1 
                AND %s = ?2
            """.formatted(
                metadata.getRelationTableName(), 
                metadata.getSourceIdField(), 
                metadata.getTargetIdField()
            );
        
        try {
            // Find the relation
            Number relationId = (Number) entityManager.createNativeQuery(findSql)
                .setParameter(1, sourceEntityId)
                .setParameter(2, targetEntityId)
                .getSingleResult();
            
            // Delete the relation
            return deleteInternalRelationById(relationId.longValue());
        } catch (NoResultException e) {
            // Relation not found
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete relation", e);
        }
    }
    
    @Override
    @Transactional
    public boolean deleteInternalRelationById(Long relationId) {
        if (relationId == null) {
            return false;
        }
        
        try {
            // Find all relation entity classes
            for (Class<? extends RelationEntity> relationEntityClass : relationRegistry.getAllRelationEntityClasses()) {
                try {
                    // Try to find and delete the relation
                    RelationEntity relation = entityManager.find(relationEntityClass, relationId);
                    if (relation != null) {
                        entityManager.remove(relation);
                        return true;
                    }
                } catch (Exception ignored) {
                    // Continue to the next relation entity class
                }
            }
            
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete relation by ID", e);
        }
    }
    
    // Helper methods
    
    /**
     * Creates relation metadata for the given entity types.
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return Relation metadata
     */
    private RelationMetadata createRelationMetadata(EntityType sourceEntityType, EntityType targetEntityType) {
        return new RelationMetadata(sourceEntityType, targetEntityType, relationRegistry);
    }
    
    /**
     * Helper class to store entity information
     */
    private static class EntityInfo {
        final Long internalId;
        final String name;
        final boolean isBound;
        
        EntityInfo(Long internalId, String name, boolean isBound) {
            this.internalId = internalId;
            this.name = name;
            this.isBound = isBound;
        }
    }
    
    /**
     * Finds entity information (internal ID and name) by external IDs in a single query
     * 
     * @param dataSource Data source
     * @param entityType Entity type
     * @param externalIds List of external IDs
     * @return Map of external ID to entity information
     */
    private Map<Long, EntityInfo> findEntityInfoByExternalIds(
        DataSource dataSource, 
        EntityType entityType, 
        List<Long> externalIds
    ) {
        if (externalIds == null || externalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        EntityMetadata metadata = new EntityMetadata(entityType);
        
        // Create placeholders for IN clause
        String placeholders = externalIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(","));
        
        // Form SQL query to get internal IDs and names in a single query
        String sql = """
            SELECT 
                b.external_id, 
                e.id AS internal_id, 
                e.name 
            FROM 
                %s b 
            JOIN 
                %s e ON b.master_id = e.id 
            WHERE 
                b.data_source_id = ? 
                AND b.external_id IN (%s)
            """.formatted(metadata.getBindingTableName(), metadata.getTableName(), placeholders);
        
        try {
            // Execute query
            Query query = entityManager.createNativeQuery(sql)
                .setParameter(1, dataSource.getCode());
            
            // Set parameters for IN clause
            for (int i = 0; i < externalIds.size(); i++) {
                query.setParameter(i + 2, externalIds.get(i));
            }
            
            List<Object[]> results = query.getResultList();
            
            // Convert results to map
            Map<Long, EntityInfo> entityInfoMap = new HashMap<>();
            for (Object[] row : results) {
                Long externalId = ((Number) row[0]).longValue();
                Long internalId = ((Number) row[1]).longValue();
                String name = (String) row[2];
                entityInfoMap.put(externalId, new EntityInfo(internalId, name, true));
            }
            
            return entityInfoMap;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find entity information", e);
        }
    }
    
    /**
     * Finds relations between source entity and target entities in a single query
     * 
     * @param sourceEntityType Source entity type
     * @param sourceInternalId Internal source entity ID
     * @param targetEntityType Target entity type
     * @param targetInternalIds List of internal target entity IDs
     * @return Map of target internal ID to relation ID
     */
    private Map<Long, Long> findRelations(
        EntityType sourceEntityType,
        Long sourceInternalId,
        EntityType targetEntityType,
        List<Long> targetInternalIds
    ) {
        if (targetInternalIds == null || targetInternalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);
        
        // Create placeholders for IN clause
        String placeholders = targetInternalIds.stream()
            .map(id -> "?")
            .collect(Collectors.joining(","));
        
        // Form SQL query to find relations
        String sql = """
            SELECT 
                id, 
                %s 
            FROM 
                %s 
            WHERE 
                %s = ? 
                AND %s IN (%s)
            """.formatted(
                metadata.getTargetIdField(),
                metadata.getRelationTableName(),
                metadata.getSourceIdField(),
                metadata.getTargetIdField(),
                placeholders
            );
        
        try {
            // Execute query
            Query query = entityManager.createNativeQuery(sql)
                .setParameter(1, sourceInternalId);
            
            // Set parameters for IN clause
            for (int i = 0; i < targetInternalIds.size(); i++) {
                query.setParameter(i + 2, targetInternalIds.get(i));
            }
            
            List<Object[]> results = query.getResultList();
            
            // Convert results to map
            Map<Long, Long> relationMap = new HashMap<>();
            for (Object[] row : results) {
                Long relationId = ((Number) row[0]).longValue();
                Long targetId = ((Number) row[1]).longValue();
                relationMap.put(targetId, relationId);
            }
            
            return relationMap;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find relations", e);
        }
    }
    
    /**
     * Finds internal entity ID by external ID and entity type
     * 
     * @param dataSource Data source
     * @param entityType Entity type
     * @param externalId External ID
     * @return Internal ID or null if entity is not found
     */
    private Long findInternalEntityId(DataSource dataSource, EntityType entityType, Long externalId) {
        EntityMetadata metadata = new EntityMetadata(entityType);
        
        String query = """
            SELECT master_id 
            FROM %s 
            WHERE data_source_id = ?1 
                AND external_id = ?2
            """.formatted(metadata.getBindingTableName());
        
        try {
            return ((Number) entityManager.createNativeQuery(query)
                .setParameter(1, dataSource.getCode())
                .setParameter(2, externalId)
                .getSingleResult()).longValue();
        } catch (NoResultException e) {
            return null;
        } catch (Exception e) {
            throw new RuntimeException("Failed to find internal entity ID", e);
        }
    }
    
    /**
     * Finds or creates a relation between entities using direct SQL
     * 
     * @param metadata Relation metadata
     * @param sourceEntityId Source entity ID
     * @param targetEntityId Target entity ID
     * @return Relation ID
     */
    private Long findOrCreateRelation(
        RelationMetadata metadata,
        Long sourceEntityId,
        Long targetEntityId
    ) {
        // Get first and second entity IDs based on relation order
        Long firstEntityId = metadata.getFirstEntityId(sourceEntityId, targetEntityId);
        Long secondEntityId = metadata.getSecondEntityId(sourceEntityId, targetEntityId);
        
        // Find existing relation
        String query = """
            SELECT 
                id 
            FROM 
                %s 
            WHERE 
                %s = ?1 
                AND %s = ?2
            """.formatted(
                metadata.getRelationTableName(), 
                metadata.getFirstEntityMetadata().getIdFieldName(), 
                metadata.getSecondEntityMetadata().getIdFieldName()
            );
        
        try {
            Number id = (Number) entityManager.createNativeQuery(query)
                .setParameter(1, firstEntityId)
                .setParameter(2, secondEntityId)
                .getSingleResult();
            return id.longValue();
        } catch (NoResultException e) {
            // Relation not found, create a new one
            try {
                // Create SQL for insertion with sequence for id
                String insertSql = """
                    INSERT INTO %s (
                        id, 
                        %s, 
                        %s, 
                        created_at, 
                        updated_at
                    ) 
                    VALUES (
                        nextval('%s_seq'), 
                        ?1, 
                        ?2, 
                        now(), 
                        now()
                    ) 
                    RETURNING id
                    """.formatted(
                        metadata.getRelationTableName(), 
                        metadata.getFirstEntityMetadata().getIdFieldName(), 
                        metadata.getSecondEntityMetadata().getIdFieldName(), 
                        metadata.getRelationTableName()
                    );
                
                // Execute insert and get generated ID
                Number id = (Number) entityManager.createNativeQuery(insertSql)
                    .setParameter(1, firstEntityId)
                    .setParameter(2, secondEntityId)
                    .getSingleResult();
                
                return id.longValue();
            } catch (Exception ex) {
                throw new RuntimeException("Failed to create relation", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to find relation", e);
        }
    }
    
    /**
     * Finds existing relation binding
     * 
     * @param metadata Relation metadata
     * @param dataSource Data source
     * @param sourceExternalEntityId External source entity ID
     * @param targetExternalEntityId External target entity ID
     * @return Optional containing the found binding
     */
    private Optional<? extends RelationBindingEntity> findExistingBinding(
        RelationMetadata metadata,
        DataSource dataSource,
        Long sourceExternalEntityId,
        Long targetExternalEntityId
    ) {
        // Get first and second external entity IDs based on relation order
        Long firstExternalEntityId = metadata.getFirstExternalEntityId(sourceExternalEntityId, targetExternalEntityId);
        Long secondExternalEntityId = metadata.getSecondExternalEntityId(sourceExternalEntityId, targetExternalEntityId);
        
        // Form SQL query
        String sql = """
            SELECT id 
            FROM %s 
            WHERE data_source_id = ?1
                AND %s = ?2
                AND %s = ?3
            """.formatted(
                metadata.getRelationBindingTableName(), 
                metadata.getFirstEntityMetadata().getExternalIdFieldName(), 
                metadata.getSecondEntityMetadata().getExternalIdFieldName()
            );
        
        try {
            // Execute query
            Long id = ((Number) entityManager.createNativeQuery(sql)
                .setParameter(1, dataSource.getCode())
                .setParameter(2, firstExternalEntityId)
                .setParameter(3, secondExternalEntityId)
                .getSingleResult()).longValue();
            
            // Load entity by ID
            return Optional.of(entityManager.find(metadata.getRelationBindingEntityClass(), id));
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Creates a new relation binding using direct SQL
     * 
     * @param metadata Relation metadata
     * @param dataSource Data source
     * @param relationId Relation ID
     * @param sourceExternalEntityId External source entity ID
     * @param targetExternalEntityId External target entity ID
     * @return Created binding
     */
    private RelationBindingEntity createBinding(
        RelationMetadata metadata,
        DataSource dataSource,
        Long relationId,
        Long sourceExternalEntityId,
        Long targetExternalEntityId
    ) {
        try {
            // Get first and second external entity IDs based on relation order
            Long firstExternalEntityId = metadata.getFirstExternalEntityId(sourceExternalEntityId, targetExternalEntityId);
            Long secondExternalEntityId = metadata.getSecondExternalEntityId(sourceExternalEntityId, targetExternalEntityId);
            
            // Create SQL for insertion with sequence for id
            String sql = """
                INSERT INTO %s (
                    id, 
                    master_id, 
                    data_source_id, 
                    %s, 
                    %s, 
                    created_at, 
                    updated_at
                ) 
                VALUES (
                    nextval('%s_seq'), 
                    ?1, 
                    ?2, 
                    ?3, 
                    ?4, 
                    now(), 
                    now()
                ) 
                RETURNING id
                """.formatted(
                    metadata.getRelationBindingTableName(), 
                    metadata.getFirstEntityMetadata().getExternalIdFieldName(), 
                    metadata.getSecondEntityMetadata().getExternalIdFieldName(), 
                    metadata.getRelationBindingTableName()
                );
            
            // Execute insert and get generated ID
            Number id = (Number) entityManager.createNativeQuery(sql)
                .setParameter(1, relationId)
                .setParameter(2, dataSource.getCode())
                .setParameter(3, firstExternalEntityId)
                .setParameter(4, secondExternalEntityId)
                .getSingleResult();
            
            // Fetch the created binding using native SQL
            return entityManager.find(metadata.getRelationBindingEntityClass(), id.longValue());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create binding", e);
        }
    }
    
    /**
     * Gets entity name by type and ID
     * 
     * @param entityType Entity type
     * @param entityId Entity ID
     * @return Entity name
     */
    private String getEntityName(EntityType entityType, Long entityId) {
        try {
            EntityMetadata metadata = new EntityMetadata(entityType);
            
            String query = """
                SELECT name 
                FROM %s 
                WHERE id = ?1
                """.formatted(metadata.getTableName());
            return (String) entityManager.createNativeQuery(query)
                .setParameter(1, entityId)
                .getSingleResult();
        } catch (Exception e) {
            return "Unknown";
        }
    }
    
    /**
     * Validates that an entity exists
     * 
     * @param entityType Entity type
     * @param entityId Entity ID
     * @throws EntityNotFoundException if entity does not exist
     */
    private void validateEntityExists(EntityType entityType, Long entityId) {
        try {
            EntityMetadata metadata = new EntityMetadata(entityType);
            
            String query = """
                SELECT COUNT(*) 
                FROM %s 
                WHERE id = ?1
                """.formatted(metadata.getTableName());
            
            Number count = (Number) entityManager.createNativeQuery(query)
                .setParameter(1, entityId)
                .getSingleResult();
            
            if (count.intValue() == 0) {
                throw new EntityNotFoundException(
                    String.format("%s with ID %d not found", entityType.getName(), entityId));
            }
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Failed to validate existence of %s with ID %d", entityType.getName(), entityId), e);
        }
    }
}
