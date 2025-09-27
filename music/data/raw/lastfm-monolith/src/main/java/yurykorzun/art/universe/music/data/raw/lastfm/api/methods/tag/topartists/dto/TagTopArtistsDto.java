package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TagTopArtistsDto {

    @JsonProperty("@attr")
    private TagTopArtistsPageInfo pageInfo;

    @JsonProperty("artist")
    private List<TagTopArtistsArtistDto> artists;

}
