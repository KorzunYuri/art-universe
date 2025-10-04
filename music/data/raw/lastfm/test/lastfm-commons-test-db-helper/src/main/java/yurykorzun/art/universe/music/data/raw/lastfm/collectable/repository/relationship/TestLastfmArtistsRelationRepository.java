package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistsRelation;

import java.util.List;

public interface TestLastfmArtistsRelationRepository extends BaseLastfmArtistsRelationRepository {

    /**
     * Find all artist relations where the specified artist is the target
     */
    @Query("SELECT ar FROM artist_artist ar WHERE ar.targetArtist.id = :targetArtistId")
    List<LastfmArtistsRelation> findByTargetArtistId(@Param("targetArtistId") Long targetArtistId);

}
