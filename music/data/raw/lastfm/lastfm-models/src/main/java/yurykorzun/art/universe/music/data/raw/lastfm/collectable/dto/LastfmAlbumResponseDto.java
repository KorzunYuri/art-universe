package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.time.LocalDateTime;

public record LastfmAlbumResponseDto(
    long id,
    String name,
    String url,
    @Nullable String mbid,
    Integer approvalStatus,
    @Nullable Long playCount,
    @Nullable Integer listenersCount,
    @Nullable LocalDateTime publishTs,
    @Nullable LastfmArtistResponseDto artist
) {

    public static LastfmAlbumResponseDto from(LastfmAlbum album) {
        return new LastfmAlbumResponseDto(
            album.getId(),
            album.getName(),
            album.getUrl(),
            album.getMbid(),
            album.getApprovalStatus().getCode(),
            album.getPlayCount(),
            album.getListenersCount(),
            album.getPublishTs(),
            album.getArtist() != null
                ? LastfmArtistResponseDto.from(album.getArtist())
                : null
        );
    }
}
