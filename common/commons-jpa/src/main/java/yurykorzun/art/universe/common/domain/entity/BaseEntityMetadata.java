package yurykorzun.art.universe.common.domain.entity;

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

    /**
     * Gets the name of the ID field for this entity in a cross-entity relation table.
     *
     * @return ID field name (e.g., "artist_id")
     */
    public String getIdFieldName() {
        return entityType.getName() + "_id";
    }

    /**
     * Gets the name of the ID field for this entity in a same-entity relation table,
     * using a prefix to distinguish source from target.
     *
     * @param prefix "source" or "target"
     * @return ID field name (e.g., "source_artist_id")
     */
    public String getIdFieldName(String prefix) {
        return prefix + "_" + entityType.getName() + "_id";
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
