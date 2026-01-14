package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import yurykorzun.art.universe.common.domain.entity.BaseEntityMetadata;

/**
 * Metadata for LastFM entities used in lookup operations
 */
public class LastfmEntityMetadata extends BaseEntityMetadata<LastfmEntityType> {
    
    public LastfmEntityMetadata(LastfmEntityType entityType) {
        super(entityType);
    }
}
