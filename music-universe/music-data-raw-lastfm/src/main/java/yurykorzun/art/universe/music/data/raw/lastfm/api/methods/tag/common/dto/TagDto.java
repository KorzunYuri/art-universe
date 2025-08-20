package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.beans.Transient;

@Data
@NoArgsConstructor
public abstract class TagDto implements EntityDto<LastfmTag> {

    private String name;

    @Override
    @Transient
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.TAG;
    }
}
