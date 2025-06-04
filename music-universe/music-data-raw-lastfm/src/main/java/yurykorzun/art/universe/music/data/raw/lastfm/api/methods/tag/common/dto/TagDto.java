package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;

@Data
@NoArgsConstructor
public class TagDto implements EntityDto, UniquenessSupport {

    private String name;

    @Override
    public String getUniqueKey() {
        return name;
    }
}
