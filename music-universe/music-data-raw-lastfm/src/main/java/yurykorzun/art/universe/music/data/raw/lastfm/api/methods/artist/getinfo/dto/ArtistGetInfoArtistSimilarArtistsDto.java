package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtistGetInfoArtistSimilarArtistsDto {

    @JsonProperty("artist")
    List<ArtistDto> artists;

}
