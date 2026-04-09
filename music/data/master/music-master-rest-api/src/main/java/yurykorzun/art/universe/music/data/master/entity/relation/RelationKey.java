package yurykorzun.art.universe.music.data.master.entity.relation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import yurykorzun.art.universe.common.domain.entity.EntityType;

/**
 * Key for identifying relation type
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode
public class RelationKey {
    private final EntityType firstEntityType;
    private final EntityType secondEntityType;
}
