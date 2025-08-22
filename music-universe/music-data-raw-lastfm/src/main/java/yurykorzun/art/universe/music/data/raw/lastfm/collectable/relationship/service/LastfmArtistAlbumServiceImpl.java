package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.service;

import jakarta.persistence.EntityManager;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistAlbum;

@Service
public class LastfmArtistAlbumServiceImpl extends AbstractEntityRelationService<LastfmArtistAlbum>
        implements LastfmArtistAlbumService {

    public LastfmArtistAlbumServiceImpl(JdbcTemplate jdbcTemplate, EntityManager entityManager) {
        super(jdbcTemplate, entityManager);
    }
}
