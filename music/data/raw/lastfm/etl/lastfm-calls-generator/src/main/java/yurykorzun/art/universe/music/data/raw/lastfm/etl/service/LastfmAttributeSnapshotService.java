package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

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
