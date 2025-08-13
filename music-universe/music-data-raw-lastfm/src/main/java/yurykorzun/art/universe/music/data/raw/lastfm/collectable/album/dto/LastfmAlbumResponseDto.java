package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;

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
