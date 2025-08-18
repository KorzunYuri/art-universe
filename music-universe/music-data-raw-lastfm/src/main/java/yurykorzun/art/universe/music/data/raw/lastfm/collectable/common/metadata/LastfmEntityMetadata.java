package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.metadata;

import yurykorzun.art.universe.common.persistence.entity.BaseEntityMetadata;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

/**
 * Metadata for LastFM entities used in lookup operations
 */
public class LastfmEntityMetadata extends BaseEntityMetadata<LastfmEntityType> {
    
    public LastfmEntityMetadata(LastfmEntityType entityType) {
        super(entityType);
    }
}
