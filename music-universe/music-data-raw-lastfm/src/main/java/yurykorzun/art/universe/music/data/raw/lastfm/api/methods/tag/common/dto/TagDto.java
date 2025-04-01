package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.WikiDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;

@Data
@NoArgsConstructor
public class TagDto implements EntityDto, UniquenessSupport {

    private String name;

    @JsonAlias({"count", "total"})
    private int count;

    private int reach;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private WikiDto wiki;

    @Override
    public String getUniqueKey() {
        return name;
    }
}
