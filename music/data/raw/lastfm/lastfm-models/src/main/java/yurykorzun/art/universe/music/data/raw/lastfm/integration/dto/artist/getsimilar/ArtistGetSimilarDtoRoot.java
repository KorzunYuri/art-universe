package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getsimilar;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class ArtistGetSimilarDtoRoot implements DtoRoot {

    @JsonProperty("similarartists")
    private ArtistGetSimilarSimilarArtistsDto rootObject;

}
