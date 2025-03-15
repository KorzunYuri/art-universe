package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;

import java.util.List;

@Data
@NoArgsConstructor
public class TopTagsDto {

    @JsonProperty("@attr")
    private PageInfo pageInfo;

    @JsonProperty("tag")
    private List<TagDto> tags;
}

