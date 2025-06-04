package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import javax.annotation.Nullable;

@Repository
public interface LastfmAttributeSnapshotRepository extends JpaRepository<LastfmAttributeSnapshot, Long> {

    /**
     * Returns scoped or non-scoped attribute snapshots, depending on whether scope entity is provided
     */
    @Query(value = """
        SELECT  s
        FROM    attribute_snapshot s
        WHERE   1=1
            AND s.attribute         = :attribute
            AND s.entityType        = :entityType
            AND (
                        (
                                :scopeEntityType    IS NULL AND :scopeEntityId  IS NULL
                            AND s.scopeEntityType   IS NULL AND s.scopeEntityId IS NULL
                        )
                    OR
                        (
                                s.scopeEntityType = :scopeEntityType
                            AND s.scopeEntityId = :scopeEntityId
                        )
                )
    """)
    LastfmAttributeSnapshot findAttributeSnapshot(
                        @Param("attribute")         LastfmAttribute     attribute,
                        @Param("entityType")        LastfmEntityType    entityType,
            @Nullable   @Param("scopeEntityType")   LastfmEntityType    scopeEntityType,
            @Nullable   @Param("scopeEntityId")     Long                scopeEntityId
    );

    default LastfmAttributeSnapshot findTypeLevelSnapshot(
            LastfmAttribute attribute,
            LastfmEntityType entityType
    ) {
        return findAttributeSnapshot(attribute, entityType, null, null);
    }

    default <T extends BaseLastfmEntity> LastfmAttributeSnapshot findEntityLevelSnapshot(
            LastfmAttribute attribute,
            LastfmEntityType entityType,
            T scopeEntity
    ) {
        return findAttributeSnapshot(attribute, entityType, scopeEntity.getType(), scopeEntity.getId());
    }
}
