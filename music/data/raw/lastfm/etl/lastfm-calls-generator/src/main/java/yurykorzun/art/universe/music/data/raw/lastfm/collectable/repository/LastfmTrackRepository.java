package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmTrackRepository extends BaseLastfmTrackRepository {

    /**
     * Find tracks for track.getInfo processing with validation at SQL level.
     * Valid tracks must have: mbid OR (name AND artist.name)
     * Optimized for performance on large datasets.
     */
    @Query(value = """
    WITH ranked_tracks AS (
        SELECT
            t.*,
            a.listeners_count       AS artist_listeners_count,
            a.approval_status       AS artist_approval_status,
            CASE t.approval_status
                WHEN 2 THEN 0
                WHEN 4 THEN 1
                WHEN 1 THEN 2
                ELSE 3 END          AS track_status_priority,
            CASE a.approval_status
                WHEN 2 THEN 0
                WHEN 4 THEN 1
                WHEN 1 THEN 2
                ELSE 3 END          AS artist_status_priority,
            EXISTS (
                SELECT 1
                FROM api_call ac
                WHERE ac.type = 10
                  AND ac.entity_type = 3
                  AND ac.entity_id = t.id
            ) AS has_api_call,
            ROW_NUMBER() OVER (
                PARTITION BY t.artist_id
                ORDER BY
                    CASE t.approval_status
                        WHEN 2 THEN 0
                        WHEN 4 THEN 1
                        WHEN 1 THEN 2
                        ELSE 3 END,
                    CASE a.approval_status
                        WHEN 2 THEN 0
                        WHEN 4 THEN 1
                        WHEN 1 THEN 2
                        ELSE 3 END,
                    COALESCE(a.listeners_count, -1) DESC,
                    t.id
            ) AS artist_track_rank
        FROM track t
        JOIN artist a ON t.artist_id = a.id
        WHERE
            a.approval_status IN (1,2,4)
            AND t.approval_status NOT IN (3,5)
            AND NOT EXISTS (
                SELECT 1 FROM blacklist_entity_url bl
                WHERE bl.entity_type = 3 AND bl.url = t.url
            )
    )
    SELECT *
    FROM ranked_tracks
    WHERE artist_track_rank <= :tracksPerArtist
    ORDER BY
        has_api_call,
        track_status_priority,
        artist_status_priority,
        COALESCE(artist_listeners_count, -1) DESC,
        id
    LIMIT :limit;
    """, nativeQuery = true)
    List<LastfmTrack> findTracksForGetInfo(
        @Param("limit") int limit,
        @Param("tracksPerArtist") int tracksPerArtist
    );

    default List<LastfmTrack> findTracksForGetInfo(int limit) {
        return findTracksForGetInfo(limit, 3);
    }

    default List<LastfmTrack> findTracksForGetInfo() {
        return findTracksForGetInfo(LastfmConstants.HIBERNATE_BATCH_SIZE);
    }
}
