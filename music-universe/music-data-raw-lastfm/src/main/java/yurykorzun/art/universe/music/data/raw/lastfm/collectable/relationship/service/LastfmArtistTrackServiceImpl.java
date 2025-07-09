package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTrack;

@Service
public class LastfmArtistTrackServiceImpl extends AbstractEntityRelationService<LastfmArtistTrack>
        implements LastfmArtistTrackService {

    public LastfmArtistTrackServiceImpl(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }
}
