package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto;

import java.util.Set;

/**
 * Search parameters for finding entity tags
 */
public record EntityTagSearchParams(
    Integer minUsageCount,
    Set<Integer> approvalStatuses
) {
}
