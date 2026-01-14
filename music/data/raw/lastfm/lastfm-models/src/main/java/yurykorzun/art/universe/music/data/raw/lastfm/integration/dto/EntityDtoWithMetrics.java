package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;

/**
 * Interface for DTOs that contain quality metrics for validation.
 * These DTOs can be validated against thresholds and automatically blacklisted if they don't meet quality standards.
 * 
 * @param <E> The entity type this DTO represents
 */
public interface EntityDtoWithMetrics<E extends BaseLastfmEntity> extends EntityDto<E> {
    
    /**
     * Returns the primary quality metric value for this entity type:
     * - listeners_count for artists
     * - play_count for tracks and albums  
     * - usage_count for tags
     */
    Number getMetricValue();
}
