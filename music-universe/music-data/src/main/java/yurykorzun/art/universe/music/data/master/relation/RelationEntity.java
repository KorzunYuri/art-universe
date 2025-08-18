package yurykorzun.art.universe.music.data.master.relation;

import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Marker interface for relation entities.
 * Implementations should provide information about entity types in the relation.
 */
public interface RelationEntity {
    /**
     * @return Type of the first entity in the relation
     */
    MasterEntityType getFirstEntityType();
    
    /**
     * @return Type of the second entity in the relation
     */
    MasterEntityType getSecondEntityType();
    
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
}
