package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.topartists;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class TagTopArtistsDtoRoot implements DtoRoot {

    @JsonProperty("topartists")
    private TagTopArtistsDto topArtists;
}
