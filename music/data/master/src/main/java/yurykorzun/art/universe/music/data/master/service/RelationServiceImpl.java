package yurykorzun.art.universe.music.data.master.service;

import jakarta.annotation.Nullable;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationBindingEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationRegistry;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityMetadata;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationMetadata;

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
    private final RelationQueryDispatcher relationQueryDispatcher;

    public RelationServiceImpl(EntityManager entityManager, RelationRegistry relationRegistry,
                               RelationQueryDispatcher relationQueryDispatcher) {
        this.entityManager = entityManager;
        this.relationRegistry = relationRegistry;
        this.relationQueryDispatcher = relationQueryDispatcher;
    }

    @Override
    @Transactional
    public RelationBindingDTO bindExternalRelation(
        DataSource dataSource, 
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
        Long targetExternalEntityId
    ) {
        // Check that external entities are bound
        Long sourceInternalId = findInternalEntityId(dataSource, sourceEntityType, sourceExternalEntityId);
        Long targetInternalId = findInternalEntityId(dataSource, targetEntityType, targetExternalEntityId);
        
        if (sourceInternalId == null) {
            throw new CustomEntityNotFoundException(
                String.format("Source external %s with id %d is not bound", sourceEntityType.getName(), sourceExternalEntityId));
        }
        
        if (targetInternalId == null) {
            throw new CustomEntityNotFoundException(
                String.format("Target external %s with id %d ", targetEntityType.getName(), targetExternalEntityId));
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
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
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
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
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
            .isSourceEntityBound(sourceInfo.isBound)
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
                .isTargetEntityBound(targetInfo.isBound)
                .isInternalRelationBound(isInternalRelationBound)
                .isExternalRelationBound(isExternalRelationBound)
                .internalRelationId(relationId)
                .build());
        }
        
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelatedEntityDTO> getRelatedEntities(
        MasterEntityType sourceEntityType,
        Long sourceEntityId,
        MasterEntityType targetEntityType,
        @Nullable Long relationTypeId
    ) {
        return relationQueryDispatcher
                .findRelatedEntities(sourceEntityType, sourceEntityId, targetEntityType, relationTypeId)
                .stream()
                .map(projection -> RelatedEntityDTO.builder()
                        .id(projection.getId())
                        .name(projection.getName())
                        .relationId(projection.getRelationId())
                        .entityType(targetEntityType)
                        .relationTypeId(projection.getRelationTypeId())
                        .relationTypeName(projection.getRelationTypeName())
                        .trackOrder(projection.getTrackOrder())
                        .build())
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public Long createInternalRelation(
        MasterEntityType sourceEntityType,
        Long sourceEntityId,
        MasterEntityType targetEntityType,
        Long targetEntityId,
        @Nullable Long relationTypeId
    ) {
        // Validate that entities exist
        validateEntityExists(sourceEntityType, sourceEntityId);
        validateEntityExists(targetEntityType, targetEntityId);

        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);

        // Reject relation type for relations that don't support types (e.g. category relations)
        if (relationTypeId != null && !metadata.isSupportsRelationTypes()) {
            throw new IllegalArgumentException(
                String.format("Relation between %s and %s does not support relation types",
                    sourceEntityType.getName(), targetEntityType.getName()));
        }

        // Validate relation type applicability if a type is specified
        if (relationTypeId != null) {
            validateRelationTypeApplicability(relationTypeId, sourceEntityType, targetEntityType);
            rejectIfSystemRelationType(relationTypeId);
        }

        // Create or find the relation
        return findOrCreateRelation(metadata, sourceEntityId, targetEntityId, relationTypeId);
    }
    
    @Override
    @Transactional
    public boolean deleteInternalRelation(
        MasterEntityType sourceEntityType,
        Long sourceEntityId,
        MasterEntityType targetEntityType,
        Long targetEntityId,
        @Nullable Long relationTypeId
    ) {
        // Reject deletion of system relation types
        if (relationTypeId != null) {
            rejectIfSystemRelationType(relationTypeId);
        }

        // Create relation metadata
        RelationMetadata metadata = createRelationMetadata(sourceEntityType, targetEntityType);

        String relationTypeCondition = "";
        if (metadata.isSupportsRelationTypes()) {
            if (relationTypeId != null) {
                relationTypeCondition = " AND relation_type_id = ?3";
            } else {
                relationTypeCondition = " AND relation_type_id IS NULL";
            }
        }

        // Form SQL query to find the relation
        String findSql = """
            SELECT id
            FROM %s
            WHERE %s = ?1
                AND %s = ?2%s
            """.formatted(
                metadata.getRelationTableName(),
                metadata.getSourceIdField(),
                metadata.getTargetIdField(),
                relationTypeCondition
            );

        try {
            // Find the relation
            var query = entityManager.createNativeQuery(findSql)
                .setParameter(1, sourceEntityId)
                .setParameter(2, targetEntityId);
            if (metadata.isSupportsRelationTypes() && relationTypeId != null) {
                query.setParameter(3, relationTypeId);
            }
            Number relationId = (Number) query.getSingleResult();

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
    private RelationMetadata createRelationMetadata(MasterEntityType sourceEntityType, MasterEntityType targetEntityType) {
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
        MasterEntityType entityType,
        List<Long> externalIds
    ) {
        if (externalIds == null || externalIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        MasterEntityMetadata metadata = new MasterEntityMetadata(entityType);
        
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
        MasterEntityType sourceEntityType,
        Long sourceInternalId,
        MasterEntityType targetEntityType,
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
    private Long findInternalEntityId(DataSource dataSource, MasterEntityType entityType, Long externalId) {
        MasterEntityMetadata metadata = new MasterEntityMetadata(entityType);
        
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
     * Finds or creates a relation between entities using direct SQL.
     * Delegates to the overloaded method with null relationTypeId.
     */
    private Long findOrCreateRelation(
        RelationMetadata metadata,
        Long sourceEntityId,
        Long targetEntityId
    ) {
        return findOrCreateRelation(metadata, sourceEntityId, targetEntityId, null);
    }

    /**
     * Finds or creates a relation between entities using direct SQL
     *
     * @param metadata Relation metadata
     * @param sourceEntityId Source entity ID
     * @param targetEntityId Target entity ID
     * @param relationTypeId Optional relation type ID
     * @return Relation ID
     */
    private Long findOrCreateRelation(
        RelationMetadata metadata,
        Long sourceEntityId,
        Long targetEntityId,
        @Nullable Long relationTypeId
    ) {
        // Get first and second entity IDs based on relation order
        Long firstEntityId = metadata.getFirstEntityId(sourceEntityId, targetEntityId);
        Long secondEntityId = metadata.getSecondEntityId(sourceEntityId, targetEntityId);

        boolean hasTypeSupport = metadata.isSupportsRelationTypes();

        // Build relation_type_id condition only for tables that support it
        String relationTypeCondition = "";
        if (hasTypeSupport) {
            if (relationTypeId != null) {
                relationTypeCondition = " AND relation_type_id = ?3";
            } else {
                relationTypeCondition = " AND relation_type_id IS NULL";
            }
        }

        // Find existing relation
        String query = """
            SELECT
                id
            FROM
                %s
            WHERE
                %s = ?1
                AND %s = ?2%s
            """.formatted(
                metadata.getRelationTableName(),
                metadata.getFirstIdField(),
                metadata.getSecondIdField(),
                relationTypeCondition
            );

        try {
            var nativeQuery = entityManager.createNativeQuery(query)
                .setParameter(1, firstEntityId)
                .setParameter(2, secondEntityId);
            if (hasTypeSupport && relationTypeId != null) {
                nativeQuery.setParameter(3, relationTypeId);
            }
            Number id = (Number) nativeQuery.getSingleResult();
            return id.longValue();
        } catch (NoResultException e) {
            // Relation not found, create a new one
            try {
                String insertSql;
                if (hasTypeSupport && relationTypeId != null) {
                    insertSql = """
                        INSERT INTO %s (
                            id,
                            %s,
                            %s,
                            relation_type_id,
                            created_at,
                            updated_at
                        )
                        VALUES (
                            nextval('%s_seq'),
                            ?1,
                            ?2,
                            ?3,
                            now(),
                            now()
                        )
                        RETURNING id
                        """.formatted(
                            metadata.getRelationTableName(),
                            metadata.getFirstIdField(),
                            metadata.getSecondIdField(),
                            metadata.getRelationTableName()
                        );
                } else {
                    insertSql = """
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
                            metadata.getFirstIdField(),
                            metadata.getSecondIdField(),
                            metadata.getRelationTableName()
                        );
                }

                var insertQuery = entityManager.createNativeQuery(insertSql)
                    .setParameter(1, firstEntityId)
                    .setParameter(2, secondEntityId);
                if (hasTypeSupport && relationTypeId != null) {
                    insertQuery.setParameter(3, relationTypeId);
                }
                Number id = (Number) insertQuery.getSingleResult();

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
    private String getEntityName(MasterEntityType entityType, Long entityId) {
        try {
            MasterEntityMetadata metadata = new MasterEntityMetadata(entityType);
            
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
     * Validates that a relation type exists and is applicable to the given entity types.
     * Uses the canonical order: lower entity type code first.
     *
     * @param relationTypeId Relation type ID
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @throws CustomEntityNotFoundException if relation type does not exist
     * @throws IllegalArgumentException if relation type is not applicable to the entity pair
     */
    private void rejectIfSystemRelationType(Long relationTypeId) {
        String sql = "SELECT is_system FROM relation_type WHERE id = ?1";
        try {
            Boolean isSystem = (Boolean) entityManager.createNativeQuery(sql)
                .setParameter(1, relationTypeId)
                .getSingleResult();
            if (Boolean.TRUE.equals(isSystem)) {
                throw new IllegalArgumentException(
                    "Cannot manually create or delete relations with system relation types. " +
                    "These relations are managed automatically.");
            }
        } catch (NoResultException e) {
            // Will be caught by validateRelationTypeApplicability
        }
    }

    private void validateRelationTypeApplicability(
        Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType
    ) {
        // Check that relation type exists
        String checkTypeSql = "SELECT COUNT(*) FROM relation_type WHERE id = ?1";
        Number typeCount = (Number) entityManager.createNativeQuery(checkTypeSql)
            .setParameter(1, relationTypeId)
            .getSingleResult();

        if (typeCount.intValue() == 0) {
            throw new CustomEntityNotFoundException("relation_type", relationTypeId);
        }

        // Check applicability in both directions since the pair may be stored in either order
        String checkApplicabilitySql = """
            SELECT COUNT(*) FROM relation_type_applicability
            WHERE relation_type_id = ?1
                AND ((source_entity_type = ?2 AND target_entity_type = ?3)
                  OR (source_entity_type = ?3 AND target_entity_type = ?2))
            """;
        Number applicabilityCount = (Number) entityManager.createNativeQuery(checkApplicabilitySql)
            .setParameter(1, relationTypeId)
            .setParameter(2, sourceEntityType.getCode())
            .setParameter(3, targetEntityType.getCode())
            .getSingleResult();

        if (applicabilityCount.intValue() == 0) {
            throw new IllegalArgumentException(
                String.format("Relation type %d is not applicable to entity pair %s -> %s",
                    relationTypeId, sourceEntityType.getName(), targetEntityType.getName()));
        }
    }

    /**
     * Validates that an entity exists
     *
     * @param entityType Entity type
     * @param entityId Entity ID
     * @throws CustomEntityNotFoundException if entity does not exist
     */
    private void validateEntityExists(MasterEntityType entityType, Long entityId) {
        try {
            MasterEntityMetadata metadata = new MasterEntityMetadata(entityType);
            
            String query = """
                SELECT COUNT(*) 
                FROM %s 
                WHERE id = ?1
                """.formatted(metadata.getTableName());
            
            Number count = (Number) entityManager.createNativeQuery(query)
                .setParameter(1, entityId)
                .getSingleResult();
            
            if (count.intValue() == 0) {
                throw new CustomEntityNotFoundException(entityType.getName(), entityId);
            }
        } catch (CustomEntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                String.format("Failed to validate existence of %s with ID %d", entityType.getName(), entityId), e);
        }
    }
}
