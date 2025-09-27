package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

public interface EntityDto<E extends BaseLastfmEntity> {
    String getName();
    LastfmEntityType getEntityType();
    String getUrl();
}
