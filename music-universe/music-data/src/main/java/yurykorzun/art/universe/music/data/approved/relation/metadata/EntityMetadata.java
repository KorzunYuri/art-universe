package yurykorzun.art.universe.music.data.approved.relation.metadata;

import lombok.Getter;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;

/**
 * Metadata for an entity, containing information about table names and field names.
 */
@Getter
public class EntityMetadata {
    private final EntityType entityType;
    private final String tableName;
    private final String bindingTableName;
    
    public EntityMetadata(EntityType entityType) {
        this.entityType = entityType;
        this.tableName = entityType.getName();
        this.bindingTableName = tableName + "_binding";
    }
    
    /**
     * Gets the name of the ID field for this entity in a relation table.
     * 
     * @return ID field name
     */
    public String getIdFieldName() {
        return entityType.getName() + "_id";
    }
    
    /**
     * Gets the name of the external ID field for this entity in a binding table.
     * 
     * @return External ID field name
     */
    public String getExternalIdFieldName() {
        return "external_" + entityType.getName() + "_id";
    }
}
