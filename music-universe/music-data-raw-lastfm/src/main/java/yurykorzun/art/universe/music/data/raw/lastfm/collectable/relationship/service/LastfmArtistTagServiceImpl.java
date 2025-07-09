package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;

@Service
public class LastfmArtistTagServiceImpl extends AbstractEntityRelationService<LastfmArtistTag>
        implements LastfmArtistTagService {

    public LastfmArtistTagServiceImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
