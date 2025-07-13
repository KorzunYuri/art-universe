package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmAlbumTag;

@Service
public class LastfmAlbumTagServiceImpl extends AbstractEntityRelationService<LastfmAlbumTag>
        implements LastfmAlbumTagService {

    public LastfmAlbumTagServiceImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
