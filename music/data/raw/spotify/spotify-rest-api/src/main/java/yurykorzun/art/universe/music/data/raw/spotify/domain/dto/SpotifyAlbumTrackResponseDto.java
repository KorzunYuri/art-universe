package yurykorzun.art.universe.music.data.raw.spotify.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;

public record SpotifyAlbumTrackResponseDto(
        long id,
        @Nullable Integer trackNumber,
        long trackId,
        String trackName,
        String spotifyId,
        @Nullable String spotifyUrl,
        @Nullable Long primaryArtistId,
        @Nullable Integer durationMs
) {
    public static SpotifyAlbumTrackResponseDto from(SpotifyTrack track) {
        return new SpotifyAlbumTrackResponseDto(
                track.getId(),
                track.getTrackNumber(),
                track.getId(),
                track.getName(),
                track.getSpotifyId(),
                track.getSpotifyUrl(),
                track.getPrimaryArtistId(),
                track.getDurationMs()
        );
    }
}
