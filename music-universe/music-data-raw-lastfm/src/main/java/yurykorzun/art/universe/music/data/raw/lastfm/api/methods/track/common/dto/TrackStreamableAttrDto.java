package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TrackStreamableAttrDto {

    @JsonProperty("#text")
    private int text;

    @JsonProperty("fulltrack")
    private int fullTrack;

}
