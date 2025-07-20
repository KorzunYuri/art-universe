package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.EntityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

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
    RelationBindingDTO bindExternalRelation(
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
    boolean unbindExternalRelation(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        Long targetExternalEntityId
    );
    
    /**
     * Finds binding status for a source entity and a list of target entities
     * 
     * @param dataSource Data source
     * @param sourceEntityType Source entity type
     * @param sourceExternalEntityId External source entity ID
     * @param targetEntityType Target entity type
     * @param targetExternalEntityIds List of external target entity IDs
     * @return DTO with binding status information
     */
    RelationBindingStatusDTO findBoundExternalRelations(
        DataSource dataSource, 
        EntityType sourceEntityType, 
        Long sourceExternalEntityId, 
        EntityType targetEntityType, 
        List<Long> targetExternalEntityIds
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
    Long createInternalRelation(
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
    boolean deleteInternalRelation(
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
    boolean deleteInternalRelationById(Long relationId);
}
