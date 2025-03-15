package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.WikiDto;

@Data
@NoArgsConstructor
public class TagDto {

    private String name;

    @JsonAlias({"count", "total"})
    private int count;

    private int reach;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private WikiDto wiki;
}
