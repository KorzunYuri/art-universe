package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track;

import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

public interface TrackDtoWithMetrics extends EntityDtoWithMetrics<LastfmTrack> {

    @Override
    default Long getMetricValue() {
        return getPlayCount();
    }

    Long getPlayCount();
}
