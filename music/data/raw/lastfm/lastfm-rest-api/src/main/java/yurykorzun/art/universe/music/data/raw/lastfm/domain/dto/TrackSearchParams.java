package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import java.util.Set;

/**
 * Search parameters for finding tracks
 */
public record TrackSearchParams(
        String search,
        Long minPlayCount,
        Long minListenersCount,
        Long artistId,
        Set<Integer> approvalStatuses,
        Long tagId
) {
}
