package yurykorzun.art.universe.music.data.raw.spotify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifySearchResultDto(
        SpotifyPagingDto<SpotifyArtistDto> artists,
        SpotifyPagingDto<SpotifyAlbumDto> albums,
        SpotifyPagingDto<SpotifyTrackDto> tracks
) {
}
