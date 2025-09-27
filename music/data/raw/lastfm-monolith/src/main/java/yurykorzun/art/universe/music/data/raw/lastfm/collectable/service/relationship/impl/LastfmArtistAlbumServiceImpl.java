package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.AbstractEntityRelationService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.LastfmArtistAlbumService;

@Service
public class LastfmArtistAlbumServiceImpl extends AbstractEntityRelationService<LastfmArtistAlbum>
        implements LastfmArtistAlbumService {

    public LastfmArtistAlbumServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
