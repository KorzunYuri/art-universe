package yurykorzun.art.universe.music.data.master.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.domain.entity.BaseEntityMetadata;

/**
 * Metadata for a master entity, containing information about table names and field names.
 */
@Getter
public class MasterEntityMetadata extends BaseEntityMetadata<MasterEntityType> {

    private final String bindingTableName;
    
    public MasterEntityMetadata(MasterEntityType entityType) {
        super(entityType);
        this.bindingTableName = getTableName() + "_binding";
    }
    
    /**
     * Gets the name of the ID field for this entity in a cross-entity relation table.
     *
     * @return ID field name (e.g., "artist_id")
     */
    public String getIdFieldName() {
        return getEntityType().getName() + "_id";
    }

    /**
     * Gets the name of the ID field for this entity in a same-entity relation table,
     * using a prefix to distinguish source from target.
     *
     * @param prefix "source" or "target"
     * @return ID field name (e.g., "source_artist_id")
     */
    public String getIdFieldName(String prefix) {
        return prefix + "_" + getEntityType().getName() + "_id";
    }

    /**
     * Gets the name of the external ID field for this entity in a binding table.
     *
     * @return External ID field name
     */
    public String getExternalIdFieldName() {
        return "external_" + getEntityType().getName() + "_id";
    }
}
