package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTrack;

@Service
public class LastfmAlbumTrackServiceImpl extends AbstractEntityRelationService<LastfmAlbumTrack>
        implements LastfmAlbumTrackService {

    public LastfmAlbumTrackServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
