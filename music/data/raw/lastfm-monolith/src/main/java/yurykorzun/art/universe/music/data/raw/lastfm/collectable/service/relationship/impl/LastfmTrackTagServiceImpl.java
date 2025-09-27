package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmTrackTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmTrackTagService;

@Service
public class LastfmTrackTagServiceImpl extends AbstractEntityRelationService<LastfmTrackTag>
        implements LastfmTrackTagService {

    public LastfmTrackTagServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
