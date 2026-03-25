package yurykorzun.art.universe.music.data.raw.spotify.domain.service.lookup;

import jakarta.persistence.EntityManager;
import yurykorzun.art.universe.common.domain.service.lookup.BaseLookupService;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

/**
 * Common lookup service for Spotify entities.
 * Uses default name-matching ordering from BaseLookupService (no approval status prioritization).
 */
public class SpotifyEntityLookupService extends BaseLookupService<SpotifyEntityType, SpotifyEntityMetadata> {

    public SpotifyEntityLookupService(EntityManager entityManager, SpotifyEntityType entityType) {
        super(entityManager, entityType);
    }

    @Override
    protected SpotifyEntityMetadata createEntityMetadata() {
        return new SpotifyEntityMetadata(getEntityType());
    }
}
