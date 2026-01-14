package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.topartists;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.RankInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopArtistsArtistDto extends ArtistDto {

    @JsonProperty("@attr")
    private RankInfo recordInfo;

    private int streamable;

}
