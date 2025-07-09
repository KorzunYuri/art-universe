package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmTrackTag;

@Service
public class LastfmTrackTagServiceImpl extends AbstractEntityRelationService<LastfmTrackTag>
        implements LastfmTrackTagService {

    public LastfmTrackTagServiceImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
