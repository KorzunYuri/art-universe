package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

/**
 * Lookup service for LastFM tags
 */
@Service
public class LastfmTagLookupService extends LastfmEntityLookupService {

    public LastfmTagLookupService(EntityManager entityManager) {
        super(entityManager, LastfmEntityType.TAG);
    }
}
