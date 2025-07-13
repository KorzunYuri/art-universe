package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;

public interface EntityDto<E extends BaseLastfmEntity> extends UniquenessSupport {
    String getName();
}
