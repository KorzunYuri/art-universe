package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import java.util.Set;

/**
 * Search parameters for finding artists
 */
public record ArtistSearchParams(
        String search,
        Long minPlayCount,
        Long minListenersCount,
        Set<Integer> approvalStatuses,
        Long tagId
) {
}
