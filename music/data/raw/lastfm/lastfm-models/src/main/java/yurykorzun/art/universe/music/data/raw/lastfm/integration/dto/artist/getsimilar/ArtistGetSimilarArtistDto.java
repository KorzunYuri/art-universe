package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getsimilar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistGetSimilarArtistDto extends ArtistDto {

    @JsonProperty("match")
    private float matchCoeff;

    @JsonProperty("streamable")
    private int streamable;

}
