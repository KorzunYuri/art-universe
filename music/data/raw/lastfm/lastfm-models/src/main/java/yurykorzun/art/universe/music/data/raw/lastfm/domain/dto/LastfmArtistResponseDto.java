package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

public record LastfmArtistResponseDto(
    long id,
    String name,
    String url,
    @Nullable String mbid,
    Integer approvalStatus,
    @Nullable Long playCount,
    @Nullable Integer listenersCount
) {

    public static LastfmArtistResponseDto from(LastfmArtist artist) {
        return new LastfmArtistResponseDto(
            artist.getId(),
            artist.getName(),
            artist.getUrl(),
            artist.getMbid(),
            artist.getApprovalStatus().getCode(),
            artist.getPlayCount(),
            artist.getListenersCount()
        );
    }
}
