package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

public interface EntityDto<E extends BaseLastfmEntity> {
    String getName();
    LastfmEntityType getEntityType();
    String getUrl();
}
