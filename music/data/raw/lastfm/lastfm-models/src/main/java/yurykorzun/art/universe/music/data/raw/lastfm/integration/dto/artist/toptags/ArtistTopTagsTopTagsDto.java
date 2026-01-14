package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptags;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistTopTagsTopTagsDto {

    @JsonProperty("tag")
    private List<ArtistTopTagsTagDto> tags;

    @JsonProperty("@attr")
    private ArtistTopTagsArtistMetadata artist;
}
