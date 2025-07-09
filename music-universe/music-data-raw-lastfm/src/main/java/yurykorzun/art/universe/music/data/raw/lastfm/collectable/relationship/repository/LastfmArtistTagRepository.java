package yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.relationship.entity.LastfmArtistTag;

import java.util.List;

public interface LastfmArtistTagRepository extends JpaRepository<LastfmArtistTag, Long> {
    
    /**
     * Find all artist-tag relations for a specific artist
     */
    @Query("SELECT at FROM artist_tag at WHERE at.artist.id = :artistId")
    List<LastfmArtistTag> findByArtistId(@Param("artistId") Long artistId);
}
