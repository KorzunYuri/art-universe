package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

import java.util.List;

public interface LastfmTrackRepository extends JpaRepository<LastfmTrack, Long> {

    /**
     * Find track by exact artist name and track name. Is used for deduplication purposes.
     */
    @Query("""
        SELECT  t
        FROM track t
        JOIN t.artist a
        WHERE   t.name = :trackName
            AND a.name = :artistName

    """)
    List<LastfmTrack> findByNameAndArtistName(@Param("trackName") String trackName, @Param("artistName") String artistName);

    List<LastfmTrack> findAllByUrlIn(List<String> urls);
}

