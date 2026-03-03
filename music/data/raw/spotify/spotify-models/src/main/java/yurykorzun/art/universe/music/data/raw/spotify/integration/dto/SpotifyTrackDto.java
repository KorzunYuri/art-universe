package yurykorzun.art.universe.music.data.raw.spotify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyTrackDto(
        String id,
        String name,
        String uri,
        @JsonProperty("duration_ms") Integer durationMs,
        @JsonProperty("track_number") Integer trackNumber,
        @JsonProperty("disc_number") Integer discNumber,
        @JsonProperty("explicit") Boolean explicit,
        @JsonProperty("is_playable") Boolean isPlayable,
        List<SpotifySimplifiedArtistDto> artists,
        @JsonProperty("external_urls") Map<String, String> externalUrls,
        @JsonProperty("external_ids") Map<String, String> externalIds
) {
    public String getSpotifyUrl() {
        return externalUrls != null ? externalUrls.get("spotify") : null;
    }

    public SpotifySimplifiedArtistDto getPrimaryArtist() {
        return artists != null && !artists.isEmpty() ? artists.getFirst() : null;
    }

    public String getIsrc() {
        return externalIds != null ? externalIds.get("isrc") : null;
    }

    public String getEan() {
        return externalIds != null ? externalIds.get("ean") : null;
    }

    public String getUpc() {
        return externalIds != null ? externalIds.get("upc") : null;
    }
}
