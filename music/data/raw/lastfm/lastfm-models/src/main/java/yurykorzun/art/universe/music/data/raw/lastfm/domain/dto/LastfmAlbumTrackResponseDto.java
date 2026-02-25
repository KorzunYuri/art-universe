package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTrack;

public record LastfmAlbumTrackResponseDto(
    long id,
    @Nullable Integer position,
    long trackId,
    String trackName,
    @Nullable String trackUrl,
    @Nullable Long trackArtistId,
    @Nullable String trackArtistName
) {

    public static LastfmAlbumTrackResponseDto from(LastfmAlbumTrack albumTrack) {
        var track = albumTrack.getTrack();
        var artist = track.getArtist();
        return new LastfmAlbumTrackResponseDto(
            albumTrack.getId(),
            albumTrack.getPosition(),
            track.getId(),
            track.getName(),
            track.getUrl(),
            artist != null ? artist.getId() : null,
            artist != null ? artist.getName() : null
        );
    }
}
