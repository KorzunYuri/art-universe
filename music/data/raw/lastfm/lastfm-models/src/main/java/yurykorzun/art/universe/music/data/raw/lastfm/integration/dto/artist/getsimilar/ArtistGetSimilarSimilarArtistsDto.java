package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getsimilar;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistGetSimilarSimilarArtistsDto {

    @JsonProperty("artist")
    private List<ArtistGetSimilarArtistDto> artists;

}
