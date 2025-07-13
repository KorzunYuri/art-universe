package yurykorzun.art.universe.music.data.approved.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.EntityDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;
import yurykorzun.art.universe.music.data.approved.service.RelationService;

import java.util.List;

/**
 * Controller for working with entity relations
 */
@RestController
@RequestMapping("/api/v1/relations")
public class RelationController {

    private final RelationService relationService;

    public RelationController(RelationService relationService) {
        this.relationService = relationService;
    }

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
    @PostMapping("/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}")
    public ResponseEntity<ResponseWrapper<RelationBindingDTO>> bindExternalRelation(
        @PathVariable DataSource dataSource,
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetExternalEntityId
    ) {
        try {
            RelationBindingDTO result = relationService.bindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to bind relation: %s", e.getMessage()));
        }
    }

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
    @DeleteMapping("/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}")
    public ResponseEntity<ResponseWrapper<Boolean>> unbindExternalRelation(
        @PathVariable DataSource dataSource,
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetExternalEntityId
    ) {
        try {
            boolean result = relationService.unbindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to unbind relation: %s", e.getMessage()));
        }
    }

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
    @GetMapping("/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}")
    public ResponseEntity<ResponseWrapper<RelationBindingStatusDTO>> findBoundExternalRelations(
        @PathVariable DataSource dataSource,
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable EntityType targetEntityType,
        @RequestParam(required = false) List<Long> targetExternalEntityIds
    ) {
        try {
            RelationBindingStatusDTO result = relationService.findBoundExternalRelations(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to get bound relations: %s", e.getMessage()));
        }
    }

    /**
     * Gets related entities for a given entity
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @return List of DTOs with related entity information
     */
    @GetMapping("/{sourceEntityType}/{sourceEntityId}/{targetEntityType}")
    public ResponseEntity<ResponseWrapper<List<EntityDTO>>> getRelatedEntities(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType
    ) {
        try {
            List<EntityDTO> result = relationService.getRelatedEntities(
                sourceEntityType, sourceEntityId, targetEntityType);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to get related entities: %s", e.getMessage()));
        }
    }
    
    /**
     * Creates an internal relation between two entities
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @param targetEntityId Target entity ID
     * @return ID of the created relation
     */
    @PostMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    public ResponseEntity<ResponseWrapper<Long>> createInternalRelation(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId
    ) {
        try {
            Long relationId = relationService.createInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
            return ResponseWrapper.success(relationId);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to create relation: %s", e.getMessage()));
        }
    }
    
    /**
     * Deletes an internal relation by entity types and IDs
     * 
     * @param sourceEntityType Source entity type
     * @param sourceEntityId Source entity ID
     * @param targetEntityType Target entity type
     * @param targetEntityId Target entity ID
     * @return true if deletion was successful, false otherwise
     */
    @DeleteMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    public ResponseEntity<ResponseWrapper<Boolean>> deleteInternalRelation(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId
    ) {
        try {
            boolean result = relationService.deleteInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to delete relation: %s", e.getMessage()));
        }
    }
    
    /**
     * Deletes an internal relation by relation ID
     * 
     * @param relationId Relation ID
     * @return true if deletion was successful, false otherwise
     */
    @DeleteMapping("/internal/{relationId}")
    public ResponseEntity<ResponseWrapper<Boolean>> deleteInternalRelationById(
        @PathVariable Long relationId
    ) {
        try {
            boolean result = relationService.deleteInternalRelationById(relationId);
            return ResponseWrapper.success(result);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to delete relation: %s", e.getMessage()));
        }
    }
}
