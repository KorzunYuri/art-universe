package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

public interface TrackDtoWithMetrics extends EntityDtoWithMetrics<LastfmTrack> {

    @Override
    default Long getMetricValue() {
        return getPlayCount();
    }

    Long getPlayCount();
}
