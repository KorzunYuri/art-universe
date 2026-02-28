package yurykorzun.art.universe.music.data.master.controller;

import jakarta.annotation.Nullable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.service.RelationService;

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
     */
    @PostMapping("/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}")
    public RelationBindingDTO bindExternalRelation(
        @PathVariable DataSource dataSource,
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable MasterEntityType targetEntityType,
        @PathVariable Long targetExternalEntityId
    ) {
        return relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
    }

    /**
     * Unbinds an external relation
     */
    @DeleteMapping("/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}")
    public boolean unbindExternalRelation(
        @PathVariable DataSource dataSource,
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable MasterEntityType targetEntityType,
        @PathVariable Long targetExternalEntityId
    ) {
        return relationService.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
    }

    /**
     * Finds binding status for a source entity and a list of target entities
     */
    @GetMapping("/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}")
    public RelationBindingStatusDTO findBoundExternalRelations(
        @PathVariable DataSource dataSource,
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable Long sourceExternalEntityId,
        @PathVariable MasterEntityType targetEntityType,
        @RequestParam(name = "ids", required = false) List<Long> targetExternalEntityIds
    ) {
        return relationService.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds);
    }

    /**
     * Gets related entities for a given entity
     */
    @GetMapping("/{sourceEntityType}/{sourceEntityId}/{targetEntityType}")
    public List<RelatedEntityDTO> getRelatedEntities(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @Nullable @RequestParam(name = "relationTypeId", required = false) Long relationTypeId
    ) {
        return relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType, relationTypeId);
    }

    /**
     * Creates an internal relation between two entities
     */
    @PostMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    public Long createInternalRelation(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId,
        @Nullable @RequestParam(name = "relationTypeId", required = false) Long relationTypeId
    ) {
        return relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, relationTypeId);
    }

    /**
     * Deletes an internal relation by entity types and IDs
     */
    @DeleteMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    public boolean deleteInternalRelation(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId,
        @Nullable @RequestParam(name = "relationTypeId", required = false) Long relationTypeId
    ) {
        return relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, relationTypeId);
    }

    /**
     * Deletes an internal relation by relation ID
     */
    @DeleteMapping("/internal/{relationId}")
    public boolean deleteInternalRelationById(
        @PathVariable Long relationId
    ) {
        return relationService.deleteInternalRelationById(relationId);
    }
}
