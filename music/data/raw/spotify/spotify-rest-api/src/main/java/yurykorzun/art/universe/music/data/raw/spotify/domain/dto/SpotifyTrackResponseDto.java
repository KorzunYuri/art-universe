package yurykorzun.art.universe.music.data.raw.spotify.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyTrack;

public record SpotifyTrackResponseDto(
        long id,
        String spotifyId,
        String name,
        @Nullable Integer durationMs,
        @Nullable Integer trackNumber,
        @Nullable Integer discNumber,
        @Nullable Boolean hasExplicitLyrics,
        @Nullable Boolean isPlayable,
        @Nullable String spotifyUrl,
        @Nullable String uri,
        @Nullable String isrc,
        @Nullable Long primaryArtistId,
        @Nullable String primaryArtistSpotifyId,
        @Nullable Long albumId,
        @Nullable String albumSpotifyId,
        Integer approvalStatus
) {
    public static SpotifyTrackResponseDto from(SpotifyTrack track) {
        return new SpotifyTrackResponseDto(
                track.getId(),
                track.getSpotifyId(),
                track.getName(),
                track.getDurationMs(),
                track.getTrackNumber(),
                track.getDiscNumber(),
                track.getHasExplicitLyrics(),
                track.getIsPlayable(),
                track.getSpotifyUrl(),
                track.getUri(),
                track.getIsrc(),
                track.getPrimaryArtistId(),
                track.getPrimaryArtistSpotifyId(),
                track.getAlbumId(),
                track.getAlbumSpotifyId(),
                track.getApprovalStatus().getCode()
        );
    }
}
