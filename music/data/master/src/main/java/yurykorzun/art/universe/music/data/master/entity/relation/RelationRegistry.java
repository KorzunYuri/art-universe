package yurykorzun.art.universe.music.data.master.entity.relation;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.AlbumCategory;
import yurykorzun.art.universe.music.data.master.entity.ArtistAlbum;
import yurykorzun.art.universe.music.data.master.entity.ArtistArtist;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.ArtistPerson;
import yurykorzun.art.universe.music.data.master.entity.ArtistTrack;
import yurykorzun.art.universe.music.data.master.entity.AlbumAlbum;
import yurykorzun.art.universe.music.data.master.entity.AlbumPerson;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;
import yurykorzun.art.universe.music.data.master.entity.TrackCategory;
import yurykorzun.art.universe.music.data.master.entity.TrackPerson;
import yurykorzun.art.universe.music.data.master.entity.TrackTrack;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Registry for entity relations
 */
@Component
public class RelationRegistry {

    private final Map<RelationKey, Class<? extends RelationEntity>> relationEntities = new HashMap<>();
    private final Set<RelationKey> typeSupportingRelations = new HashSet<>();

    @PostConstruct
    public void init() {
        // Register ArtistCategory
        registerRelationEntity(ArtistCategory.class);

        // Register AlbumCategory
        registerRelationEntity(AlbumCategory.class);

        // Register TrackCategory
        registerRelationEntity(TrackCategory.class);

        // Register ArtistTrack
        registerRelationEntity(ArtistTrack.class);

        // Register ArtistAlbum
        registerRelationEntity(ArtistAlbum.class);

        // Register AlbumTrack
        registerRelationEntity(AlbumTrack.class);

        // Register same-entity relations
        registerRelationEntity(ArtistArtist.class);
        registerRelationEntity(AlbumAlbum.class);
        registerRelationEntity(TrackTrack.class);

        // Register cross-domain person relations
        registerRelationEntity(ArtistPerson.class);
        registerRelationEntity(AlbumPerson.class);
        registerRelationEntity(TrackPerson.class);
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
            if (instance.supportsRelationTypes()) {
                typeSupportingRelations.add(key);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to register relation entity: " + entityClass.getName(), e);
        }
    }

    /**
     * Returns the relation entity class for the given entity types
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
     * Checks if the source entity is the first entity in the relation
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
     * Returns all registered relation entity classes
     */
    public Collection<Class<? extends RelationEntity>> getAllRelationEntityClasses() {
        return relationEntities.values();
    }

    /**
     * Checks if the relation between the given entity types supports relation types.
     */
    public boolean supportsRelationTypes(EntityType sourceEntityType, EntityType targetEntityType) {
        RelationKey directKey = new RelationKey(sourceEntityType, targetEntityType);
        if (typeSupportingRelations.contains(directKey)) {
            return true;
        }
        RelationKey reverseKey = new RelationKey(targetEntityType, sourceEntityType);
        return typeSupportingRelations.contains(reverseKey);
    }
}
