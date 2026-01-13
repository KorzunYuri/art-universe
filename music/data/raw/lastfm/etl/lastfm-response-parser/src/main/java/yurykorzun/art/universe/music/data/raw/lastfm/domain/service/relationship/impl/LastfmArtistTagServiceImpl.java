package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmArtistTagService;

@Service
public class LastfmArtistTagServiceImpl extends AbstractEntityRelationService<LastfmArtistTag>
        implements LastfmArtistTagService {

    public LastfmArtistTagServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
