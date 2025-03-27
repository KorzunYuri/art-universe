package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

public interface LastfmAttributeSnapshotService {

    LastfmAttributeSnapshot getOrCreateForEntityType(
            LastfmDataSnapshot snapshot,
            LastfmEntityType entityType,
            LastfmAttribute attribute);

    <T extends BaseLastfmEntity> LastfmAttributeSnapshot getOrCreateForEntity(
            LastfmDataSnapshot snapshot,
            LastfmEntityType entityType,
            LastfmAttribute attribute,
            T scopeEntity
    );
}
