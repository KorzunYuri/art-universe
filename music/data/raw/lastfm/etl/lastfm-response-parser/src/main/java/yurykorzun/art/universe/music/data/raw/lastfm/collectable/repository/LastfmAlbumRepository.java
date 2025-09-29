package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends BaseLastfmAlbumRepository {

    /**
     * Find album by exact artist name and album name. Is used for deduplication purposes.
     */
    @Query("""
        SELECT al
        FROM
            album al
        JOIN
            al.artist ar
        WHERE   al.name = :albumName
            AND ar.name = :artistName
    """)
    List<LastfmAlbum> findByNameAndArtistName(@Param("albumName") String albumName, @Param("artistName") String artistName);

    List<LastfmAlbum> findAllByUrlIn(List<String> urls);

}
