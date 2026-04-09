package yurykorzun.art.universe.music.data.master.entity.relation;

import yurykorzun.art.universe.common.domain.entity.EntityType;

/**
 * Marker interface for relation entities.
 * Implementations should provide information about entity types in the relation.
 */
public interface RelationEntity {
    /**
     * @return Type of the first entity in the relation
     */
    EntityType getFirstEntityType();

    /**
     * @return Type of the second entity in the relation
     */
    EntityType getSecondEntityType();
    
    /**
     * @return ID of the first entity
     */
    Long getFirstEntityId();
    
    /**
     * @return ID of the second entity
     */
    Long getSecondEntityId();
    
    /**
     * @return ID of the relation
     */
    Long getId();

    /**
     * @return ID of the relation type, or null if untyped
     */
    default Long getRelationTypeId() {
        return null;
    }

    /**
     * @return true if this relation supports relation types (has relation_type_id column)
     */
    default boolean supportsRelationTypes() {
        return false;
    }
}
