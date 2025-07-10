package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

public record LastfmTrackResponseDto(
    long id,
    String name,
    String url,
    @Nullable String mbid,
    Integer approvalStatus,
    @Nullable Integer listenersCount,
    @Nullable Long playCount,
    @Nullable LastfmArtistResponseDto artist
) {

    public static LastfmTrackResponseDto from(LastfmTrack track) {
        return new LastfmTrackResponseDto(
            track.getId(),
            track.getName(),
            track.getUrl(),
            track.getMbid(),
            track.getApprovalStatus().getCode(),
            track.getListenersCount(),
            track.getPlayCount(),
            track.getArtist() != null
                ? LastfmArtistResponseDto.from(track.getArtist())
                : null
        );
    }
}
