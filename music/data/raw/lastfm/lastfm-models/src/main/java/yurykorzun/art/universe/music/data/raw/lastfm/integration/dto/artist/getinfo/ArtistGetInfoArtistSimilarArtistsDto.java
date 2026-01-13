package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtistGetInfoArtistSimilarArtistsDto {

    @JsonProperty("artist")
    List<ArtistGetInfoSimilarArtistDto> artists;

}
