package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ArtistGetInfoArtistTagDto extends TagDto {

    @JsonProperty("url")
    private String url;

}
