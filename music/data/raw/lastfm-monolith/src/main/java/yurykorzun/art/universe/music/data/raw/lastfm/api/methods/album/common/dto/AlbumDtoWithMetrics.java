package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

public interface AlbumDtoWithMetrics extends EntityDtoWithMetrics<LastfmAlbum> {

    @Override
    default Number getMetricValue() {
        return getPlayCount();
    }

    Long getPlayCount();
}
