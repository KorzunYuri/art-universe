package yurykorzun.art.universe.music.data.raw.spotify.domain.service.lookup;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Service
public class SpotifyArtistLookupService extends SpotifyEntityLookupService {

    public SpotifyArtistLookupService(EntityManager entityManager) {
        super(entityManager, SpotifyEntityType.ARTIST);
    }
}
