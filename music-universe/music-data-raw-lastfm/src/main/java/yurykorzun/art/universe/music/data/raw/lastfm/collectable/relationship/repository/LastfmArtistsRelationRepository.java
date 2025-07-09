package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistsRelation;

import java.util.List;
import java.util.Optional;

public interface LastfmArtistsRelationRepository extends JpaRepository<LastfmArtistsRelation, Long> {
    
    /**
     * Find all artist relations where the specified artist is the target
     */
    @Query("SELECT ar FROM artist_artist ar WHERE ar.targetArtist.id = :targetArtistId")
    List<LastfmArtistsRelation> findByTargetArtistId(@Param("targetArtistId") Long targetArtistId);
    
    /**
     * Find all artist relations where the specified artist is the source
     */
    @Query("SELECT ar FROM artist_artist ar WHERE ar.sourceArtist.id = :sourceArtistId")
    List<LastfmArtistsRelation> findBySourceArtistId(@Param("sourceArtistId") Long sourceArtistId);
    
    /**
     * Find a specific artist relation by source and target artist IDs
     * 
     * @param sourceArtistId The ID of the source artist
     * @param targetArtistId The ID of the target artist
     * @return Optional containing the relation if found
     */
    @Query("SELECT ar FROM artist_artist ar WHERE ar.sourceArtist.id = :sourceArtistId AND ar.targetArtist.id = :targetArtistId")
    Optional<LastfmArtistsRelation> findBySourceArtistIdAndTargetArtistId(
        @Param("sourceArtistId") Long sourceArtistId,
        @Param("targetArtistId") Long targetArtistId
    );
}
