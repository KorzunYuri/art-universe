package yurykorzun.art.universe.music.data.master.controller;

import jakarta.annotation.Nullable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import yurykorzun.art.universe.music.data.master.service.RelationService;

import java.util.List;

/**
 * Internal API for ETL relation creation.
 * All relations created through this controller have origin=ETL.
 */
@RestController
@RequestMapping("/api/v1/internal/relations")
public class InternalRelationController {

    private final RelationService relationService;

    public InternalRelationController(RelationService relationService) {
        this.relationService = relationService;
    }

    @PostMapping("/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}")
    public List<Long> createInternalRelations(
        @PathVariable EntityType sourceEntityType,
        @PathVariable Long sourceEntityId,
        @PathVariable EntityType targetEntityType,
        @PathVariable Long targetEntityId,
        @Nullable @RequestParam(name = "relationTypeIds", required = false) List<Long> relationTypeIds,
        @RequestParam(defaultValue = "pending") MasterApprovalStatus approvalStatus
    ) {
        return relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            relationTypeIds, Origin.ETL, approvalStatus);
    }
}
