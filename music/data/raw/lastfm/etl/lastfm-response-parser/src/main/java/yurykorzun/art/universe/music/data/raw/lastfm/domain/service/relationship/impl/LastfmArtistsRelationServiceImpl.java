package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistsRelationService;

@Service
public class LastfmArtistsRelationServiceImpl extends AbstractEntityRelationService<LastfmArtistsRelation>
        implements LastfmArtistsRelationService {

    public LastfmArtistsRelationServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
