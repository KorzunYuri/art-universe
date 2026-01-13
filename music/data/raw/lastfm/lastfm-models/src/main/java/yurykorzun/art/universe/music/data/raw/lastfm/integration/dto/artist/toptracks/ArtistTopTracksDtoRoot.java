package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptracks;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class ArtistTopTracksDtoRoot implements DtoRoot {

    @JsonProperty("toptracks")
    ArtistTopTracksTopTracksDto rootObject;
}
