package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TrackGetInfoTagDto extends TagDto {

    @JsonProperty("url")
    private String url;
}
