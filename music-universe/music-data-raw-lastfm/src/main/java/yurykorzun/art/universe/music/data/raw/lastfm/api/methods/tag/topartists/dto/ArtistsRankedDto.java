package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RankInfo;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ArtistsRankedDto extends ArtistDto {

    @JsonProperty("@attr")
    private RankInfo recordInfo;

}
