package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.TagDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class TagTopTagsTagDto extends TagDto implements TagDtoWithMetrics {

    @JsonProperty("count")
    private Integer usageCount;

    @JsonProperty("reach")
    private Integer usageUsersCount;

    // doesn't exist in DTO, calculated by processor on the fly
    @Setter
    private Integer rank;


    /**
     * This DTO is an exception from the rule that entity DTOs have URLs.
     * However, as long as URL is used for quality check and is not applicable to this DTO,
     * this method can remain unused but guarded by the exception.
     */
    @Override
    public String getUrl() {
        throw new UnsupportedOperationException("Tags from tag.topTags don't have URL");
    }
}
