package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

/**
 * Interface for artist DTOs that contain quality metrics (listeners_count).
 * These DTOs can be validated against thresholds and automatically blacklisted.
 */
public interface ArtistDtoWithMetrics extends EntityDtoWithMetrics<LastfmArtist> {

    @Override
    default Integer getMetricValue() {
        return getListenersCount();
    }

    /**
     * @return Number of listeners for this artist
     */
    Integer getListenersCount();
}
