package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.DtoRoot;

@Data
@NoArgsConstructor
public class TagTopTagsDtoRoot implements DtoRoot {

    @JsonProperty("toptags")
    private TagTopTagsTagsDto topTags;

}
