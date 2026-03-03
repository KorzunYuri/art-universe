package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

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
}
