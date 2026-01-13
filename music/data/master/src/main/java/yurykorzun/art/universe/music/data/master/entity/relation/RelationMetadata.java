package yurykorzun.art.universe.music.data.master.entity.relation;

import lombok.Getter;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityMetadata;

/**
 * Metadata for a relation between two entities, containing information about
 * relation table names, field names, and entity order.
 */
@Getter
public class RelationMetadata {
    private final MasterEntityMetadata sourceMetadata;
    private final MasterEntityMetadata targetMetadata;
    private final boolean isSourceFirst;
    private final String relationTableName;
    private final String relationBindingTableName;
    private final RelationRegistry registry;
    
    /**
     * Creates relation metadata for the given source and target entity types.
     * 
     * @param sourceType Source entity type
     * @param targetType Target entity type
     * @param registry Relation registry
     */
    public RelationMetadata(MasterEntityType sourceType, MasterEntityType targetType, RelationRegistry registry) {
        this.registry = registry;
        this.isSourceFirst = registry.isFirstEntityInRelation(sourceType, targetType);
        
        MasterEntityType firstType = isSourceFirst ? sourceType : targetType;
        MasterEntityType secondType = isSourceFirst ? targetType : sourceType;
        
        this.sourceMetadata = new MasterEntityMetadata(sourceType);
        this.targetMetadata = new MasterEntityMetadata(targetType);
        
        this.relationTableName = firstType.getName() + "_" + secondType.getName();
        this.relationBindingTableName = relationTableName + "_binding";
    }
    
    /**
     * Gets the metadata for the first entity in the relation.
     * 
     * @return First entity metadata
     */
    public MasterEntityMetadata getFirstEntityMetadata() {
        return isSourceFirst ? sourceMetadata : targetMetadata;
    }
    
    /**
     * Gets the metadata for the second entity in the relation.
     * 
     * @return Second entity metadata
     */
    public MasterEntityMetadata getSecondEntityMetadata() {
        return isSourceFirst ? targetMetadata : sourceMetadata;
    }
    
    /**
     * Gets the name of the source ID field in the relation table.
     * 
     * @return Source ID field name
     */
    public String getSourceIdField() {
        return isSourceFirst ? 
            sourceMetadata.getIdFieldName() : 
            targetMetadata.getIdFieldName();
    }
    
    /**
     * Gets the name of the target ID field in the relation table.
     * 
     * @return Target ID field name
     */
    public String getTargetIdField() {
        return isSourceFirst ? 
            targetMetadata.getIdFieldName() : 
            sourceMetadata.getIdFieldName();
    }
    
    /**
     * Gets the name of the source external ID field in the binding table.
     * 
     * @return Source external ID field name
     */
    public String getSourceExternalIdField() {
        return isSourceFirst ? 
            sourceMetadata.getExternalIdFieldName() : 
            targetMetadata.getExternalIdFieldName();
    }
    
    /**
     * Gets the name of the target external ID field in the binding table.
     * 
     * @return Target external ID field name
     */
    public String getTargetExternalIdField() {
        return isSourceFirst ? 
            targetMetadata.getExternalIdFieldName() : 
            sourceMetadata.getExternalIdFieldName();
    }
    
    /**
     * Gets the relation entity class for this relation.
     * 
     * @return Relation entity class
     */
    public Class<? extends RelationEntity> getRelationEntityClass() {
        return registry.getRelationEntityClass(
            getFirstEntityMetadata().getEntityType(), 
            getSecondEntityMetadata().getEntityType()
        );
    }
    
    /**
     * Gets the relation binding entity class for this relation.
     * 
     * @return Relation binding entity class
     */
    public Class<? extends RelationBindingEntity> getRelationBindingEntityClass() {
        return registry.getRelationBindingEntityClass(
            getFirstEntityMetadata().getEntityType(), 
            getSecondEntityMetadata().getEntityType()
        );
    }
    
    /**
     * Gets the first entity ID based on whether the source is first.
     * 
     * @param sourceId Source entity ID
     * @param targetId Target entity ID
     * @return First entity ID
     */
    public Long getFirstEntityId(Long sourceId, Long targetId) {
        return isSourceFirst ? sourceId : targetId;
    }
    
    /**
     * Gets the second entity ID based on whether the source is first.
     * 
     * @param sourceId Source entity ID
     * @param targetId Target entity ID
     * @return Second entity ID
     */
    public Long getSecondEntityId(Long sourceId, Long targetId) {
        return isSourceFirst ? targetId : sourceId;
    }
    
    /**
     * Gets the first external entity ID based on whether the source is first.
     * 
     * @param sourceExternalId Source external entity ID
     * @param targetExternalId Target external entity ID
     * @return First external entity ID
     */
    public Long getFirstExternalEntityId(Long sourceExternalId, Long targetExternalId) {
        return isSourceFirst ? sourceExternalId : targetExternalId;
    }
    
    /**
     * Gets the second external entity ID based on whether the source is first.
     * 
     * @param sourceExternalId Source external entity ID
     * @param targetExternalId Target external entity ID
     * @return Second external entity ID
     */
    public Long getSecondExternalEntityId(Long sourceExternalId, Long targetExternalId) {
        return isSourceFirst ? targetExternalId : sourceExternalId;
    }
}
