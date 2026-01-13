package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

/**
 * Lookup service for LastFM artists
 */
@Service
public class LastfmArtistLookupService extends LastfmEntityLookupService {

    public LastfmArtistLookupService(EntityManager entityManager) {
        super(entityManager, LastfmEntityType.ARTIST);
    }
}
