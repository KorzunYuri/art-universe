package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.dto;

import java.util.Set;

/**
 * Search parameters for finding tags
 */
public record TagSearchParams(
        String search,
        Set<Integer> approvalStatuses
) {
}
