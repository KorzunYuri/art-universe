package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ArtistGetInfoArtistTagsDto {

    @JsonProperty("tag")
    private List<ArtistGetInfoArtistTagDto> tags;

}
