package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.toptags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ArtistTopTagsTagDto extends TagDto implements TagDtoWithMetrics {

    @JsonProperty("url")
    private String url;

    @JsonProperty("count")
    private Integer usageCount;
}
