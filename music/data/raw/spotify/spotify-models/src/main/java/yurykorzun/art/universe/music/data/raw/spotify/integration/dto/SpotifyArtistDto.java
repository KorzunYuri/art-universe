package yurykorzun.art.universe.music.data.raw.spotify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyArtistDto(
        String id,
        String name,
        String uri,
        String type,
        List<String> genres,
        @JsonProperty("external_urls") Map<String, String> externalUrls
) {
    public String getSpotifyUrl() {
        return externalUrls != null ? externalUrls.get("spotify") : null;
    }
}
