package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RootDto;

@Data
@NoArgsConstructor
public class TagTopArtistsDtoRoot implements RootDto {

    @JsonProperty("topartists")
    private TagTopArtistsDto topArtists;
}
