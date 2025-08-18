package yurykorzun.art.universe.music.data.master.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

/**
 * Key for identifying relation type
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class RelationKey {
    private final MasterEntityType firstEntityType;
    private final MasterEntityType secondEntityType;
}
