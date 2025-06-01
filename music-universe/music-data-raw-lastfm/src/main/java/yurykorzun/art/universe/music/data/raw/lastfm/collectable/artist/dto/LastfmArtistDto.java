package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

public record LastfmArtistDto (
    long id,
    String name,
    String url,
    @Nullable String mbid,
    ApprovalStatus approvalStatus,
    @Nullable Integer playCount,
    @Nullable Integer listenersCount
) {

    public static LastfmArtistDto from(LastfmArtist artist) {
        return new LastfmArtistDto(
            artist.getId(),
            artist.getName(),
            artist.getUrl(),
            artist.getMbid(),
            artist.getApprovalStatus(),
            artist.getPlayCount(),
            artist.getListenersCount()
        );
    }
}
