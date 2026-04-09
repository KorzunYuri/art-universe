package yurykorzun.art.universe.music.data.master.controller;

import jakarta.annotation.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
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
     * Creates internal relations between two entities.
     * Accepts multiple relation type IDs; when none provided, creates a single untyped relation.
     */
    @PostMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    @PreAuthorize("hasRole('MASTER_CURATOR')")
    public List<Long> createInternalRelations(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId,
        @Nullable @RequestParam(name = "relationTypeIds", required = false) List<Long> relationTypeIds
    ) {
        return relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, relationTypeIds,
            Origin.MANUAL, MasterApprovalStatus.APPROVED);
    }

    /**
     * Deletes an internal relation by entity types and IDs
     */
    @DeleteMapping("/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    @PreAuthorize("hasRole('MASTER_CURATOR')")
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
     * Deletes an internal relation by relation ID, targeting the specific relation table
     * identified by source and target entity types.
     */
    @DeleteMapping("/internal/{sourceEntityType}/{targetEntityType}/{relationId}")
    @PreAuthorize("hasRole('MASTER_CURATOR')")
    public boolean deleteInternalRelationById(
        @PathVariable EntityType sourceEntityType,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long relationId
    ) {
        return relationService.deleteInternalRelationById(relationId, sourceEntityType, targetEntityType);
    }
}
