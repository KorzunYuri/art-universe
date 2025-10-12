package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends BaseLastfmAlbumRepository {

    /**
     * Find albums for album.getInfo method call with the following priority:
     * 1. Albums with missing listenersCount
     * 2. Orphaned albums (without artist_id)
     * 3. Albums from popular artists (join by artist_id, sort by listenersCount)
     * Excludes albums that are blacklisted.
     * Includes albums without artist_id (orphaned albums).
     */
    @Query(value = """
    WITH ranked_albums AS (
        SELECT
            a.*,
            ar.listeners_count AS artist_listeners_count,
            ar.approval_status AS artist_approval_status,
            CASE 
                WHEN a.artist_id IS NULL THEN 1  -- orphaned albums always rank 1
                ELSE ROW_NUMBER() OVER (
                    PARTITION BY a.artist_id
                    ORDER BY a.id
                )
            END AS artist_album_rank
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
            AND NOT EXISTS (
                SELECT 1
                FROM blacklist_entity_url bl
                WHERE bl.entity_type = 2        -- ALBUM
                  AND bl.url = a.url
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
        CASE WHEN ra.artist_id IS NULL
                THEN 0  -- prioritize orphaned albums
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
}
