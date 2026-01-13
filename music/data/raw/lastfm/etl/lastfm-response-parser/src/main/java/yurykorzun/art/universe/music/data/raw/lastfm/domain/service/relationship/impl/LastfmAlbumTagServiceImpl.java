package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.LastfmAlbumTagService;

@Service
public class LastfmAlbumTagServiceImpl extends AbstractEntityRelationService<LastfmAlbumTag>
        implements LastfmAlbumTagService {

    public LastfmAlbumTagServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
