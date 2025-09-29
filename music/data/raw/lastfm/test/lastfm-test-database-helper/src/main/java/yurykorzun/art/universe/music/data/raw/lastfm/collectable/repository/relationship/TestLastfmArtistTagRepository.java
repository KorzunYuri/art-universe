package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;

import java.util.List;

public interface TestLastfmArtistTagRepository extends BaseLastfmArtistTagRepository {

    /**
     * Find all artist-tag relations for a specific artist
     */
    @Query("SELECT at FROM artist_tag at WHERE at.artist.id = :artistId")
    List<LastfmArtistTag> findByArtistId(@Param("artistId") Long artistId);

}
