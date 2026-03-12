package yurykorzun.art.universe.music.data.raw.spotify.domain.dto;

import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.SpotifyGenre;

public record SpotifyGenreResponseDto(
        long id,
        String spotifyId,
        String name,
        Integer approvalStatus
) {
    public static SpotifyGenreResponseDto from(SpotifyGenre genre) {
        return new SpotifyGenreResponseDto(
                genre.getId(),
                genre.getSpotifyId(),
                genre.getName(),
                genre.getApprovalStatus().getCode()
        );
    }
}
