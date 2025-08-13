package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto;

import java.util.Set;

public record AlbumSearchParams(
    String search,
    Long minPlayCount,
    Long minListenersCount,
    Long artistId,
    Set<Integer> approvalStatuses,
    Long tagId
) {
}
