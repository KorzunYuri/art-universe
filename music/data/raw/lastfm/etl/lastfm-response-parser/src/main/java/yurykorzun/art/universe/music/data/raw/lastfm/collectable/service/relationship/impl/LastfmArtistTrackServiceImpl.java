package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistTrackService;

@Service
public class LastfmArtistTrackServiceImpl extends AbstractEntityRelationService<LastfmArtistTrack>
        implements LastfmArtistTrackService {

    public LastfmArtistTrackServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
