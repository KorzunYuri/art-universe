package yurykorzun.art.universe.music.data.raw.spotify.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyArtist;

public record SpotifyArtistResponseDto(
        long id,
        String spotifyId,
        String name,
        @Nullable String spotifyUrl,
        @Nullable String uri,
        Integer approvalStatus
) {
    public static SpotifyArtistResponseDto from(SpotifyArtist artist) {
        return new SpotifyArtistResponseDto(
                artist.getId(),
                artist.getSpotifyId(),
                artist.getName(),
                artist.getSpotifyUrl(),
                artist.getUri(),
                artist.getApprovalStatus().getCode()
        );
    }
}
