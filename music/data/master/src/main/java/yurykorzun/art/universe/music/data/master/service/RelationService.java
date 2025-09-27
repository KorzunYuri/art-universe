package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

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
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
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
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
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
        MasterEntityType sourceEntityType,
        Long sourceExternalEntityId, 
        MasterEntityType targetEntityType,
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
    List<RelatedEntityDTO> getRelatedEntities(
        MasterEntityType sourceEntityType,
        Long sourceEntityId, 
        MasterEntityType targetEntityType
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
        MasterEntityType sourceEntityType,
        Long sourceEntityId,
        MasterEntityType targetEntityType,
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
        MasterEntityType sourceEntityType,
        Long sourceEntityId,
        MasterEntityType targetEntityType,
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
