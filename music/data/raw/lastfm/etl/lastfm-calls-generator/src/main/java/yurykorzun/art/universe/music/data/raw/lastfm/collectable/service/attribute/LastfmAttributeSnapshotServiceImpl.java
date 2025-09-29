package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeSnapshotRepository;

@Service
public class LastfmAttributeSnapshotServiceImpl implements LastfmAttributeSnapshotService {

    private final LastfmAttributeSnapshotRepository attributeSnapshotRepository;

    public LastfmAttributeSnapshotServiceImpl(LastfmAttributeSnapshotRepository attributeSnapshotRepository) {
        this.attributeSnapshotRepository = attributeSnapshotRepository;
    }

    @Override
    public LastfmAttributeSnapshot getOrCreateForEntityType(
            LastfmDataSnapshot snapshot,
            LastfmEntityType entityType,
            LastfmAttribute attribute
    ) {
        //  create or update snapshot
        boolean isUpdated;
        LastfmAttributeSnapshot attributeSnapshot = attributeSnapshotRepository.findTypeLevelSnapshot(attribute, entityType);
        if (attributeSnapshot == null) {
            attributeSnapshot = builderForType(snapshot, attribute, entityType).build();
            isUpdated = true;
        } else {
            isUpdated = updateSnapshotIfNeeded(attributeSnapshot, snapshot);
        }

        //  save snapshot if changed
        if (isUpdated) {
            attributeSnapshot = attributeSnapshotRepository.save(attributeSnapshot);
        }
        return attributeSnapshot;
    }

    @Override
    public <T extends BaseLastfmEntity> LastfmAttributeSnapshot getOrCreateForEntity(
            LastfmDataSnapshot snapshot,
            LastfmEntityType entityType,
            LastfmAttribute attribute,
            T scopeEntity
    ) {
        //  create or update snapshot
        boolean isUpdated;
        LastfmAttributeSnapshot attributeSnapshot = attributeSnapshotRepository.findEntityLevelSnapshot(attribute, entityType, scopeEntity);
        if (attributeSnapshot == null) {
            attributeSnapshot = builderForType(snapshot, attribute, entityType)
                    .scopeEntityType(scopeEntity.getType())
                    .scopeEntityId(scopeEntity.getId())
                .build();
            isUpdated = true;
        } else {
            isUpdated = updateSnapshotIfNeeded(attributeSnapshot, snapshot);
        }

        //  save snapshot if changed
        if (isUpdated) {
            attributeSnapshot = attributeSnapshotRepository.save(attributeSnapshot);
        }
        return attributeSnapshot;
    }

    private LastfmAttributeSnapshot.LastfmAttributeSnapshotBuilder<?, ?> builderForType(
            LastfmDataSnapshot snapshot, LastfmAttribute attribute, LastfmEntityType entityType) {
        return LastfmAttributeSnapshot.builder()
                .currentSnapshotId(snapshot.getId())
                .entityType(entityType)
                .attribute(attribute);
    }


    private boolean updateSnapshotIfNeeded(LastfmAttributeSnapshot attributeSnapshot, LastfmDataSnapshot snapshot) {
        if (snapshot.getId() != attributeSnapshot.getCurrentSnapshotId()) {
            attributeSnapshot.setNewSnapshot(snapshot);
            return true;
        }
        return false;
    }
}
