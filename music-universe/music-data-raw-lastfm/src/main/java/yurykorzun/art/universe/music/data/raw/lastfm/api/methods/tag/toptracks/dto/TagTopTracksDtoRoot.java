package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.RootDto;

@Data
@NoArgsConstructor
public class TagTopTracksDtoRoot implements RootDto {

    @JsonProperty("tracks")
    private TagTopTracksTracksDto rootObject;

}
