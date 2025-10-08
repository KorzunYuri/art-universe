package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.DtoRoot;

@Data
@NoArgsConstructor
public class TagTopTagsDtoRoot implements DtoRoot {

    @JsonProperty("toptags")
    private TagTopTagsTagsDto topTags;

}
