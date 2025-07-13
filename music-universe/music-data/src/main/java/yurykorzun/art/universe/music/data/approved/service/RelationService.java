package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.EntityDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationPair;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;

import java.util.List;

/**
 * Service for working with entity relations
 */
public interface RelationService {
    /**
     * Binds an external relation to an internal one
     * 
     * @param dataSource Data source
     * @param sourceEntityType Source entity type
     * @param sourceExternalEntityId External source entity ID
     * @param targetEntityType Target entity type
     * @param targetExternalEntityId External target entity ID
     * @return DTO with binding information
     */
    RelationBindingDTO bindRelation(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        Long targetExternalEntityId
    );
    
    /**
     * Unbinds an external relation
     * 
     * @param dataSource Data source
     * @param sourceEntityType Source entity type
     * @param sourceExternalEntityId External source entity ID
     * @param targetEntityType Target entity type
     * @param targetExternalEntityId External target entity ID
     * @return true if unbinding was successful, false otherwise
     */
    boolean unbindRelation(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        Long targetExternalEntityId
    );
    
    /**
     * Finds bound relations by a list of external ID pairs
     * 
     * @param dataSource Data source
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @param pairs List of external entity ID pairs
     * @return List of DTOs with binding information
     */
    List<RelationBindingDTO> findBoundRelations(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        EntityType targetEntityType, 
        List<RelationPair> pairs
    );
    
    /**
     * Gets related entities for a given entity
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @return List of DTOs with related entity information
     */
    List<EntityDTO> getRelatedEntities(
        EntityType sourceEntityType, 
        Long sourceEntityId, 
        EntityType targetEntityType
    );
    
    /**
     * Creates an internal relation between two entities
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @param targetEntityId Target entity ID
     * @return ID of the created relation
     */
    Long createRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId
    );
    
    /**
     * Deletes an internal relation by entity types and IDs
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @param targetEntityId Target entity ID
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId
    );
    
    /**
     * Deletes an internal relation by relation ID
     * 
     * @param relationId Relation ID
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteRelationById(Long relationId);
}
