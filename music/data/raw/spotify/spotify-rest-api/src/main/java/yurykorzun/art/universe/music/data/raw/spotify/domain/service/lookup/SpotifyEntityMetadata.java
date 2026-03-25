package yurykorzun.art.universe.music.data.raw.spotify.domain.service.lookup;

import yurykorzun.art.universe.common.domain.entity.BaseEntityMetadata;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

public class SpotifyEntityMetadata extends BaseEntityMetadata<SpotifyEntityType> {

    public SpotifyEntityMetadata(SpotifyEntityType entityType) {
        super(entityType);
    }
}
