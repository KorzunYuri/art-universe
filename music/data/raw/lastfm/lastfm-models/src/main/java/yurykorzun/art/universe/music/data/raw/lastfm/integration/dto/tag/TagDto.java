package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.beans.Transient;

@Data
@NoArgsConstructor
public abstract class TagDto implements EntityDto<LastfmTag> {

    @JsonProperty("name")
    private String name;

    @Override
    @Transient
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.TAG;
    }
}
