package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;

@Data
@NoArgsConstructor
public class ArtistSearchDtoRoot implements DtoRoot {

    @JsonProperty("results")
    private ArtistSearchResultsDto rootObject;

}
