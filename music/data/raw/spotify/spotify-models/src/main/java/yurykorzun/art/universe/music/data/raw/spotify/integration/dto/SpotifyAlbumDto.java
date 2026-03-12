package yurykorzun.art.universe.music.data.raw.spotify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyAlbumDto(
        String id,
        String name,
        String uri,
        @JsonProperty("album_type") String albumType,
        @JsonProperty("total_tracks") Integer totalTracks,
        @JsonProperty("release_date") String releaseDate,
        @JsonProperty("release_date_precision") String releaseDatePrecision,
        List<SpotifySimplifiedArtistDto> artists,
        @JsonProperty("external_urls") Map<String, String> externalUrls,
        SpotifyPagingDto<SpotifyTrackDto> tracks
) {
    public String getSpotifyUrl() {
        return externalUrls != null ? externalUrls.get("spotify") : null;
    }

    public SpotifySimplifiedArtistDto getPrimaryArtist() {
        return artists != null && !artists.isEmpty() ? artists.getFirst() : null;
    }
}
