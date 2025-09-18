package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
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

    @Query(value = """
        SELECT  t
        FROM    track t
        LEFT JOIN FETCH t.artist
        WHERE   1=1
            AND ((LOWER(t.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR t.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR t.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR t.artist.id = :artistId)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM track_tag tt 
                WHERE tt.track.id = t.id AND tt.tag.id = :tagId
            ))
        """)
    Page<LastfmTrack> findTracksWithoutApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    @Query(value = """
        SELECT  t
        FROM    track t
        LEFT JOIN FETCH t.artist
        WHERE   1=1
            AND ((LOWER(t.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR t.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR t.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR t.artist.id = :artistId)
            AND t.approvalStatus IN (:approvalStatuses)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM track_tag tt 
                WHERE tt.track.id = t.id AND tt.tag.id = :tagId
            ))
        """)
    Page<LastfmTrack> findTracksWithApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    /**
     * A wrapper for findTracks for correct collection parameters resolution.
     * This implementation avoids Hibernate bugs with:
     * 1. Null String parameters being recognized as bytea
     * 2. Empty collections handling
     * 3. Ensures null values are sorted last for numeric fields
     */
    default Page<LastfmTrack> findTracks(
        String search,
        Long minPlayCount,
        Long minListenersCount,
        Long artistId,
        List<ApprovalStatus> approvalStatuses,
        Long tagId,
        Pageable pageable
    ) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findTracksWithoutApprovalStatus(search, minPlayCount, minListenersCount, artistId, tagId, pageable);
        } else {
            return findTracksWithApprovalStatus(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId, pageable);
        }
    }

    /**
     * Find tracks with missing playCount and listenersCount that haven't been processed by track.getInfo yet.
     * Excludes tracks that are blacklisted.
     */
    @Query(value = """
    WITH ranked_tracks AS (
        SELECT
            t.*,
            a.listeners_count AS artist_listeners_count,
            a.approval_status AS artist_approval_status,
            ROW_NUMBER() OVER (
                PARTITION BY t.artist_id
                ORDER BY t.id
            ) AS artist_track_rank
        FROM
            track t
        JOIN
            artist a ON t.artist_id = a.id
        WHERE   1=1
            AND a.approval_status in (2, 4) -- approved, pre-approved
            AND NOT EXISTS (
                SELECT 1
                FROM api_call ac
                WHERE ac.type           = 10    -- track.getInfo
                  AND ac.entity_type    = 3     -- track
                  AND ac.entity_id      = t.id
                  AND ac.due_dttm > CURRENT_TIMESTAMP
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blacklist_entity_url bl
                WHERE bl.entity_type = 3        -- TRACK
                  AND bl.url = t.url
            )
    )
    SELECT
        rt.*
    FROM
        ranked_tracks rt
    WHERE
        rt.artist_track_rank <= :tracksPerArtist
    ORDER BY
        CASE WHEN rt.listeners_count IS NULL
                THEN 0
                ELSE 1 END,
        CASE WHEN rt.artist_approval_status = 2 -- APPROVED
                THEN 0
             WHEN rt.artist_approval_status = 4 -- PRE-APPROVED
                THEN 1
                ELSE 2 END,
        COALESCE(rt.artist_listeners_count, -1) DESC,
        rt.id
    LIMIT :limit
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
