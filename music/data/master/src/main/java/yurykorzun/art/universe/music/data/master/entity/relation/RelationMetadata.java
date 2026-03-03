package yurykorzun.art.universe.music.data.master.entity.relation;

import lombok.Getter;
import yurykorzun.art.universe.common.domain.entity.BaseEntityMetadata;
import yurykorzun.art.universe.common.domain.entity.EntityType;

/**
 * Metadata for a relation between two entities, containing information about
 * relation table names, field names, and entity order.
 * Supports both cross-entity relations (artist_track) and same-entity relations (artist_artist).
 */
@Getter
public class RelationMetadata {
    private final BaseEntityMetadata<EntityType> sourceMetadata;
    private final BaseEntityMetadata<EntityType> targetMetadata;
    private final boolean isSourceFirst;
    private final boolean isSameEntityRelation;
    private final boolean supportsRelationTypes;
    private final String relationTableName;
    private final RelationRegistry registry;

    /**
     * Creates relation metadata for the given source and target entity types.
     *
     * @param sourceType Source entity type
     * @param targetType Target entity type
     * @param registry Relation registry
     */
    public RelationMetadata(EntityType sourceType, EntityType targetType, RelationRegistry registry) {
        this.registry = registry;
        this.isSameEntityRelation = sourceType == targetType;
        this.isSourceFirst = isSameEntityRelation || registry.isFirstEntityInRelation(sourceType, targetType);
        this.supportsRelationTypes = registry.supportsRelationTypes(sourceType, targetType);

        EntityType firstType = isSourceFirst ? sourceType : targetType;
        EntityType secondType = isSourceFirst ? targetType : sourceType;

        this.sourceMetadata = new BaseEntityMetadata<>(sourceType);
        this.targetMetadata = new BaseEntityMetadata<>(targetType);

        this.relationTableName = firstType.getName() + "_" + secondType.getName();
    }

    /**
     * Gets the metadata for the first entity in the relation.
     */
    public BaseEntityMetadata<EntityType> getFirstEntityMetadata() {
        return isSourceFirst ? sourceMetadata : targetMetadata;
    }

    /**
     * Gets the metadata for the second entity in the relation.
     */
    public BaseEntityMetadata<EntityType> getSecondEntityMetadata() {
        return isSourceFirst ? targetMetadata : sourceMetadata;
    }

    /**
     * Gets the name of the source ID field in the relation table.
     * For same-entity relations, uses "source_" prefix.
     */
    public String getSourceIdField() {
        if (isSameEntityRelation) {
            return sourceMetadata.getIdFieldName("source");
        }
        return isSourceFirst ?
            sourceMetadata.getIdFieldName() :
            targetMetadata.getIdFieldName();
    }

    /**
     * Gets the name of the target ID field in the relation table.
     * For same-entity relations, uses "target_" prefix.
     */
    public String getTargetIdField() {
        if (isSameEntityRelation) {
            return targetMetadata.getIdFieldName("target");
        }
        return isSourceFirst ?
            targetMetadata.getIdFieldName() :
            sourceMetadata.getIdFieldName();
    }

    /**
     * Gets the name of the first entity ID field in the relation table.
     * For same-entity relations, this is the "source_" prefixed field.
     */
    public String getFirstIdField() {
        if (isSameEntityRelation) {
            return sourceMetadata.getIdFieldName("source");
        }
        return getFirstEntityMetadata().getIdFieldName();
    }

    /**
     * Gets the name of the second entity ID field in the relation table.
     * For same-entity relations, this is the "target_" prefixed field.
     */
    public String getSecondIdField() {
        if (isSameEntityRelation) {
            return targetMetadata.getIdFieldName("target");
        }
        return getSecondEntityMetadata().getIdFieldName();
    }

    /**
     * Gets the relation entity class for this relation.
     */
    public Class<? extends RelationEntity> getRelationEntityClass() {
        return registry.getRelationEntityClass(
            getFirstEntityMetadata().getEntityType(),
            getSecondEntityMetadata().getEntityType()
        );
    }

    /**
     * Gets the first entity ID based on whether the source is first.
     */
    public Long getFirstEntityId(Long sourceId, Long targetId) {
        return isSourceFirst ? sourceId : targetId;
    }

    /**
     * Gets the second entity ID based on whether the source is first.
     */
    public Long getSecondEntityId(Long sourceId, Long targetId) {
        return isSourceFirst ? targetId : sourceId;
    }
}
