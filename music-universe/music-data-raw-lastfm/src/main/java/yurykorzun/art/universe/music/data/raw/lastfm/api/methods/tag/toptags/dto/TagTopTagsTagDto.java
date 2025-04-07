package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopTagsTagDto extends TagDto {

    @JsonProperty("count")
    private int count;

    @JsonProperty("reach")
    private int reach;

    // doesn't exist in DTO, calculated by processor on the fly
    @Setter
    private int rank;
}
