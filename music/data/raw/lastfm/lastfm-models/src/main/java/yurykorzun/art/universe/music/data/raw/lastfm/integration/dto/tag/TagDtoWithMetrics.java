package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag;

import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;

public interface TagDtoWithMetrics extends EntityDtoWithMetrics<LastfmTag> {

    @Override
    default Integer getMetricValue() {
        return getUsageCount();
    }

    Integer getUsageCount();

}
