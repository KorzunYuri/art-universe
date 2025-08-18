package yurykorzun.art.universe.music.data.master.relation;

import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Marker interface for relation binding entities.
 * Implementations should provide information about entity types and external IDs.
 */
public interface RelationBindingEntity {
    /**
     * @return Type of the first entity in the relation
     */
    MasterEntityType getFirstEntityType();
    
    /**
     * @return Type of the second entity in the relation
     */
    MasterEntityType getSecondEntityType();
    
    /**
     * @return ID of the external first entity
     */
    Long getExternalFirstEntityId();
    
    /**
     * @return ID of the external second entity
     */
    Long getExternalSecondEntityId();
    
    /**
     * @return Data source
     */
    DataSource getDataSource();
    
    /**
     * @return ID of the internal relation
     */
    Long getMasterBindingId();
    
    /**
     * @return ID of the binding
     */
    Long getId();
    
    /**
     * Sets the ID of the internal relation
     * @param masterId ID of the internal relation
     */
    void setMasterBindingId(Long masterId);
}
