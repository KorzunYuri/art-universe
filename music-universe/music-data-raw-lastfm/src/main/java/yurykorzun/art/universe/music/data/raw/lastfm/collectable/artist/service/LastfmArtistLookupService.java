package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityLookupService;

/**
 * Lookup service for LastFM artists
 */
@Service
public class LastfmArtistLookupService extends LastfmEntityLookupService {

    public LastfmArtistLookupService(EntityManager entityManager) {
        super(entityManager, LastfmEntityType.ARTIST);
    }
}
