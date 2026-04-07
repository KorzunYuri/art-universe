package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Shared DTO for both "bio" (artist.getInfo) and "wiki" (album/track.getInfo) sections.
 * Both have the same structure: published, summary, content.
 */
@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WikiDto {
    @JsonProperty("published")
    private String published;

    @JsonProperty("summary")
    private String summary;

    @JsonProperty("content")
    private String content;
}
