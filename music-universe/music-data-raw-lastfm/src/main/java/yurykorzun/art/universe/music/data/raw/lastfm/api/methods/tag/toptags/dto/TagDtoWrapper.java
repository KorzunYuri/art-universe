package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;

/**
 * Decorator for {@link TagDto} providing additional attribute (RANK), calculated on the fly.
 */
public record TagDtoWrapper(TagDto dto, int rank) implements EntityDto {

    @Override
    public String getName() {
        return dto.getName();
    }

    @Override
    public String getUniqueKey() {
        return dto.getUniqueKey();
    }
}
