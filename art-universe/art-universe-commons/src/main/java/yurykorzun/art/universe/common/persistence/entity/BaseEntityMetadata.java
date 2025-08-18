package yurykorzun.art.universe.common.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Parametrized base implementation of entity metadata for lookup operations
 * @param <T> The entity type this metadata represents
 */
@Getter
@AllArgsConstructor
public class BaseEntityMetadata<T extends EntityType> {
    
    private final T entityType;
    
    public String getTableName() {
        return entityType.getName().toLowerCase();
    }
}
