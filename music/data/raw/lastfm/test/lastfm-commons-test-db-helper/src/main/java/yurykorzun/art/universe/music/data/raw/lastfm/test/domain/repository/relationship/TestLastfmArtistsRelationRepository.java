package yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.relationship.BaseLastfmArtistsRelationRepository;

import java.util.List;

public interface TestLastfmArtistsRelationRepository extends BaseLastfmArtistsRelationRepository {

    /**
     * Find all artist relations where the specified artist is the target
     */
    @Query("SELECT ar FROM artist_artist ar WHERE ar.targetArtist.id = :targetArtistId")
    List<LastfmArtistsRelation> findByTargetArtistId(@Param("targetArtistId") Long targetArtistId);

}
