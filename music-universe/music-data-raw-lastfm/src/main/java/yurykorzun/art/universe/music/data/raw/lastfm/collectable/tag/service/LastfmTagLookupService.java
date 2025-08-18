package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityLookupService;

/**
 * Lookup service for LastFM tags
 */
@Service
public class LastfmTagLookupService extends LastfmEntityLookupService {

    public LastfmTagLookupService(EntityManager entityManager) {
        super(entityManager, LastfmEntityType.TAG);
    }
}
