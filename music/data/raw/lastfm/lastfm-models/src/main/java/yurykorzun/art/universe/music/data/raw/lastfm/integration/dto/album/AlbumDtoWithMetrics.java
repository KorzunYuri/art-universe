package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album;

import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;

public interface AlbumDtoWithMetrics extends EntityDtoWithMetrics<LastfmAlbum> {

    @Override
    default Number getMetricValue() {
        return getPlayCount();
    }

    Long getPlayCount();
}
