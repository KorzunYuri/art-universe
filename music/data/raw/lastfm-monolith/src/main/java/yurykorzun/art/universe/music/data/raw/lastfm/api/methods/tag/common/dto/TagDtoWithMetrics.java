package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;

public interface TagDtoWithMetrics extends EntityDtoWithMetrics<LastfmTag> {

    @Override
    default Integer getMetricValue() {
        return getUsageCount();
    }

    Integer getUsageCount();

}
