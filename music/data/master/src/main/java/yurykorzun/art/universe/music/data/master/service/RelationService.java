package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.List;

/**
 * Service for working with entity relations
 */
public interface RelationService {
    /**
     * Binds an external relation to an internal one
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
        @Nullable Long relationTypeId
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
     * Deletes an internal relation by relation ID
     */
    boolean deleteInternalRelationById(Long relationId);
}
