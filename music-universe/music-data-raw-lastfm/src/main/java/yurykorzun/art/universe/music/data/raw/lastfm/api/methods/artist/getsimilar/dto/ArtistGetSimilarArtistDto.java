package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

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
