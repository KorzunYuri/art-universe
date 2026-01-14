package yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.relationship.BaseLastfmArtistTagRepository;

import java.util.List;

public interface TestLastfmArtistTagRepository extends BaseLastfmArtistTagRepository {

    /**
     * Find all artist-tag relations for a specific artist
     */
    @Query("SELECT at FROM artist_tag at WHERE at.artist.id = :artistId")
    List<LastfmArtistTag> findByArtistId(@Param("artistId") Long artistId);

}
