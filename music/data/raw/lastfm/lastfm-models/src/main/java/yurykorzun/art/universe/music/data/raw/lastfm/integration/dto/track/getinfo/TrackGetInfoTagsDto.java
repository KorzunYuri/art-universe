package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class TrackGetInfoTagsDto {

    @JsonProperty("tag")
    private List<TrackGetInfoTagDto> tags;
}
