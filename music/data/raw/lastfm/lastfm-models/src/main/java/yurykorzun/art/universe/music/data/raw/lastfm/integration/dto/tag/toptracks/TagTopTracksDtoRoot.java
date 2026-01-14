package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class TagTopTracksDtoRoot implements DtoRoot {

    @JsonProperty("tracks")
    private TagTopTracksTracksDto rootObject;

}
