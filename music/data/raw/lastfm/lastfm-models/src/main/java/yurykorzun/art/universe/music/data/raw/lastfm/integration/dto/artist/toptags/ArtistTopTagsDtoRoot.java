package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class ArtistTopTagsDtoRoot implements DtoRoot {

    @JsonProperty("toptags")
    private ArtistTopTagsTopTagsDto topTagsObject;

}
