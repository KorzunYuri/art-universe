package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;

@Data
@NoArgsConstructor
public class ArtistGetInfoDtoRoot implements DtoRoot {

    @JsonProperty("artist")
    private ArtistGetInfoArtistDto artist;

}
