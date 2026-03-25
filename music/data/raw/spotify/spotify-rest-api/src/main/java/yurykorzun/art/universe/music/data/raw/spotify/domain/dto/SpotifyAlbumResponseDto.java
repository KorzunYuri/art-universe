package yurykorzun.art.universe.music.data.raw.spotify.domain.dto;

import jakarta.annotation.Nullable;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyAlbum;

public record SpotifyAlbumResponseDto(
        long id,
        String spotifyId,
        String name,
        @Nullable Integer albumType,
        @Nullable Integer totalTracks,
        @Nullable String releaseDate,
        @Nullable Integer releaseDatePrecision,
        @Nullable String spotifyUrl,
        @Nullable String uri,
        @Nullable Long primaryArtistId,
        @Nullable String primaryArtistSpotifyId,
        @Nullable String primaryArtistName,
        Integer approvalStatus
) {
    public static SpotifyAlbumResponseDto from(SpotifyAlbum album) {
        return from(album, null);
    }

    public static SpotifyAlbumResponseDto from(SpotifyAlbum album, @Nullable String primaryArtistName) {
        return new SpotifyAlbumResponseDto(
                album.getId(),
                album.getSpotifyId(),
                album.getName(),
                album.getAlbumType() != null ? album.getAlbumType().getCode() : null,
                album.getTotalTracks(),
                album.getReleaseDate(),
                album.getReleaseDatePrecision() != null ? album.getReleaseDatePrecision().getCode() : null,
                album.getSpotifyUrl(),
                album.getUri(),
                album.getPrimaryArtistId(),
                album.getPrimaryArtistSpotifyId(),
                primaryArtistName,
                album.getApprovalStatus().getCode()
        );
    }
}
