package yurykorzun.art.universe.music.data.master.relation;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.ArtistTrack;
import yurykorzun.art.universe.music.data.master.entity.ArtistTrackBinding;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Registry for entity relations
 */
@Component
public class RelationRegistry {

    private final Map<RelationKey, Class<? extends RelationEntity>> relationEntities = new HashMap<>();
    private final Map<RelationKey, Class<? extends RelationBindingEntity>> relationBindingEntities = new HashMap<>();

    @PostConstruct
    public void init() {
        // Register ArtistCategory
        registerRelationEntity(ArtistCategory.class);
        registerRelationBindingEntity(ArtistCategoryBinding.class);
        
        // Register ArtistTrack
        registerRelationEntity(ArtistTrack.class);
        registerRelationBindingEntity(ArtistTrackBinding.class);
    }
    
    /**
     * Registers a relation entity class
     * 
     * @param entityClass Relation entity class
     */
    private void registerRelationEntity(Class<? extends RelationEntity> entityClass) {
        try {
            RelationEntity instance = entityClass.getDeclaredConstructor().newInstance();
            RelationKey key = new RelationKey(instance.getFirstEntityType(), instance.getSecondEntityType());
            relationEntities.put(key, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register relation entity: " + entityClass.getName(), e);
        }
    }
    
    /**
     * Registers a relation binding entity class
     * 
     * @param entityClass Relation binding entity class
     */
    private void registerRelationBindingEntity(Class<? extends RelationBindingEntity> entityClass) {
        try {
            RelationBindingEntity instance = entityClass.getDeclaredConstructor().newInstance();
            RelationKey key = new RelationKey(instance.getFirstEntityType(), instance.getSecondEntityType());
            relationBindingEntities.put(key, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register relation binding entity: " + entityClass.getName(), e);
        }
    }
    
    /**
     * Returns the relation entity class for the given entity types
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return Relation entity class
     * @throws IllegalArgumentException if no relation is found
     */
    public Class<? extends RelationEntity> getRelationEntityClass(EntityType sourceEntityType, EntityType targetEntityType) {
        // Check direct order
        RelationKey directKey = new RelationKey(sourceEntityType, targetEntityType);
        Class<? extends RelationEntity> entityClass = relationEntities.get(directKey);
        
        if (entityClass != null) {
            return entityClass;
        }
        
        // Check reverse order
        RelationKey reverseKey = new RelationKey(targetEntityType, sourceEntityType);
        entityClass = relationEntities.get(reverseKey);
        
        if (entityClass != null) {
            return entityClass;
        }
        
        throw new IllegalArgumentException(
            String.format("No relation entity found for types: %s and %s", sourceEntityType, targetEntityType));
    }
    
    /**
     * Returns the relation binding entity class for the given entity types
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return Relation binding entity class
     * @throws IllegalArgumentException if no relation binding is found
     */
    public Class<? extends RelationBindingEntity> getRelationBindingEntityClass(EntityType sourceEntityType, EntityType targetEntityType) {
        // Check direct order
        RelationKey directKey = new RelationKey(sourceEntityType, targetEntityType);
        Class<? extends RelationBindingEntity> entityClass = relationBindingEntities.get(directKey);
        
        if (entityClass != null) {
            return entityClass;
        }
        
        // Check reverse order
        RelationKey reverseKey = new RelationKey(targetEntityType, sourceEntityType);
        entityClass = relationBindingEntities.get(reverseKey);
        
        if (entityClass != null) {
            return entityClass;
        }
        
        throw new IllegalArgumentException(
            String.format("No relation binding entity found for types: %s and %s", sourceEntityType, targetEntityType));
    }
    
    /**
     * Checks if the source entity is the first entity in the relation
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return true if the source entity is the first in the relation, false otherwise
     */
    public boolean isFirstEntityInRelation(EntityType sourceEntityType, EntityType targetEntityType) {
        RelationKey directKey = new RelationKey(sourceEntityType, targetEntityType);
        if (relationEntities.containsKey(directKey)) {
            return true;
        }
        
        RelationKey reverseKey = new RelationKey(targetEntityType, sourceEntityType);
        return !relationEntities.containsKey(reverseKey);
    }
    
    /**
     * Returns the relation table name for the given entity types
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return Relation table name
     */
    public String getRelationTableName(EntityType sourceEntityType, EntityType targetEntityType) {
        try {
            Class<? extends RelationEntity> entityClass = getRelationEntityClass(sourceEntityType, targetEntityType);
            RelationEntity instance = entityClass.getDeclaredConstructor().newInstance();
            return instance.getFirstEntityType().getName() + "_" + instance.getSecondEntityType().getName();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get relation table name", e);
        }
    }
    
    /**
     * Returns the relation binding table name for the given entity types
     * 
     * @param sourceEntityType Source entity type
     * @param targetEntityType Target entity type
     * @return Relation binding table name
     */
    public String getRelationBindingTableName(EntityType sourceEntityType, EntityType targetEntityType) {
        try {
            Class<? extends RelationBindingEntity> entityClass = getRelationBindingEntityClass(sourceEntityType, targetEntityType);
            RelationBindingEntity instance = entityClass.getDeclaredConstructor().newInstance();
            return instance.getFirstEntityType().getName() + "_" + instance.getSecondEntityType().getName() + "_binding";
        } catch (Exception e) {
            throw new RuntimeException("Failed to get relation binding table name", e);
        }
    }
    
    /**
     * Returns all registered relation entity classes
     * 
     * @return Collection of relation entity classes
     */
    public Collection<Class<? extends RelationEntity>> getAllRelationEntityClasses() {
        return relationEntities.values();
    }
}
