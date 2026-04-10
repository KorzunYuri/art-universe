package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.model.DataSource;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
import jakarta.annotation.Nullable;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import java.util.List;

/**
 * Service for working with entity relations
 */
public interface RelationService {
    /**
     * Finds binding status for a source entity and a list of target entities
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
     */
    List<RelatedEntityDTO> getRelatedEntities(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        @Nullable Long relationTypeId
    );

    /**
     * Creates an internal relation between two entities
     */
    Long createInternalRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId,
        @Nullable Long relationTypeId,
        Origin origin,
        MasterApprovalStatus approvalStatus
    );

    /**
     * Creates internal relations between two entities for the given relation type IDs.
     * When relationTypeIds is null or empty, creates a single untyped relation.
     */
    List<Long> createInternalRelations(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId,
        @Nullable List<Long> relationTypeIds,
        Origin origin,
        MasterApprovalStatus approvalStatus
    );

    /**
     * Deletes an internal relation by entity types and IDs
     */
    boolean deleteInternalRelation(
        EntityType sourceEntityType,
        Long sourceEntityId,
        EntityType targetEntityType,
        Long targetEntityId,
        @Nullable Long relationTypeId
    );

    /**
     * Deletes an internal relation by relation ID, targeting the specific relation table
     * identified by source and target entity types.
     */
    boolean deleteInternalRelationById(Long relationId, EntityType sourceEntityType, EntityType targetEntityType);
}
