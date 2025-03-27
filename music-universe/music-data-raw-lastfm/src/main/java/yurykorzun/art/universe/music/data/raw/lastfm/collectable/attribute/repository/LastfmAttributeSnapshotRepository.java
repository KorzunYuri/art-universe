package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import javax.annotation.Nullable;

@Repository
public interface LastfmAttributeSnapshotRepository extends JpaRepository<LastfmAttributeSnapshot, Long> {

    LastfmAttributeSnapshot findByAttributeAndEntityTypeAndScopeEntityTypeAndScopeEntityId(
            LastfmAttribute attribute,
            LastfmEntityType entityType,
            LastfmEntityType scopeEntityType, @Nullable Long scopeEntityId
    );

    default LastfmAttributeSnapshot findTypeLevelSnapshot(
            LastfmAttribute attribute,
            LastfmEntityType entityType
    ) {
        return findByAttributeAndEntityTypeAndScopeEntityTypeAndScopeEntityId(
                attribute, entityType, null, null);
    }

    default <T extends BaseLastfmEntity> LastfmAttributeSnapshot findEntityLevelSnapshot(
            LastfmAttribute attribute,
            LastfmEntityType entityType,
            T scopeEntity
    ) {
        return findByAttributeAndEntityTypeAndScopeEntityTypeAndScopeEntityId(
                attribute,
                entityType,
                (LastfmEntityType) scopeEntity.getType(), scopeEntity.getId());
    }
}
