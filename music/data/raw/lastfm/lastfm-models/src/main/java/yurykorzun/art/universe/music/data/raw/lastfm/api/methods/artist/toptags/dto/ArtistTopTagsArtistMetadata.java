package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ArtistTopTagsArtistMetadata {

    @JsonProperty("artist")
    private String name;

}
