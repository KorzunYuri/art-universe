package yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends JpaRepository<LastfmAlbum, Long> {

    List<LastfmAlbum> findAllByUrlIn(List<String> urls);

    /**
     * Find albums with missing playCount and listenersCount that haven't been processed by album.getInfo yet
     */
    @Query(value = """
    WITH ranked_albums AS (
        SELECT
            a.*,
            ar.listeners_count AS artist_listeners_count,
            ar.approval_status AS artist_approval_status,
            ROW_NUMBER() OVER (
                PARTITION BY a.artist_id
                ORDER BY a.id
            ) AS artist_album_rank
        FROM
            album a
        LEFT JOIN
            artist ar ON a.artist_id = ar.id
        WHERE
            NOT EXISTS (
                SELECT 1
                FROM api_call ac
                WHERE ac.type           = 11    -- album.getInfo
                  AND ac.entity_type    = 2     -- album
                  AND ac.entity_id      = a.id
                  AND ac.due_dttm > CURRENT_TIMESTAMP
            )
    )
    SELECT
        ra.*
    FROM
        ranked_albums ra
    WHERE
        ra.artist_album_rank <= :albumsPerArtist
    ORDER BY
        CASE WHEN ra.listeners_count IS NULL
                THEN 0
                ELSE 1 END,
        CASE WHEN ra.artist_approval_status = 2 -- APPROVED
                THEN 0
                ELSE 1 END,
        COALESCE(ra.artist_listeners_count, -1) DESC,
        ra.id
    LIMIT :limit
    """, nativeQuery = true)
    List<LastfmAlbum> findAlbumsForGetInfo(
        @Param("limit") int limit,
        @Param("albumsPerArtist") int albumsPerArtist
    );

    default List<LastfmAlbum> findAlbumsForGetInfo(int limit) {
        return findAlbumsForGetInfo(limit, 3);
    }

    default List<LastfmAlbum> findAlbumsForGetInfo() {
        return findAlbumsForGetInfo(LastfmConstants.HIBERNATE_BATCH_SIZE);
    }

    @Query("""
        SELECT  a.url
        FROM    album a
        WHERE   a.url in :urls
    """)
    List<String> findExistingUrls(@Param("urls") List<String> strings);
}
