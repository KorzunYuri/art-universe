package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;

@Service
public class LastfmArtistsRelationServiceImpl extends AbstractEntityRelationService<LastfmArtistsRelation>
        implements LastfmArtistsRelationService {

    public LastfmArtistsRelationServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
