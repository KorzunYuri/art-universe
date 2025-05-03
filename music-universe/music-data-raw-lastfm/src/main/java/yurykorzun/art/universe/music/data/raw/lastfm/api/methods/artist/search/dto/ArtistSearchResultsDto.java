package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistSearchResultsDto {

    @JsonProperty("artistmatches")
    private ArtistSearchMatchesDto matches;

}
