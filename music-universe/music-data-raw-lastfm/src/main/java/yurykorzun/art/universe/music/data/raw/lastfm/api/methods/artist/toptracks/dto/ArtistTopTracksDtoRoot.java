package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RootDto;

@Data
@NoArgsConstructor
public class ArtistTopTracksDtoRoot implements RootDto {

    @JsonProperty("toptracks")
    ArtistTopTracksTopTracksDto rootObject;
}
