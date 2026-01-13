package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

/**
 * Lookup service for LastFM tags
 */
@Service
public class LastfmTagLookupService extends LastfmEntityLookupService {

    public LastfmTagLookupService(EntityManager entityManager) {
        super(entityManager, LastfmEntityType.TAG);
    }
}
