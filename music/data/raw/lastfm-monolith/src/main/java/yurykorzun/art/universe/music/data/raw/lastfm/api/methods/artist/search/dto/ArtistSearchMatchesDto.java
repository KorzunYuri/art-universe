package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtistSearchMatchesDto {

    @JsonProperty("artist")
    private List<ArtistSearchArtistDto> artists;

}
