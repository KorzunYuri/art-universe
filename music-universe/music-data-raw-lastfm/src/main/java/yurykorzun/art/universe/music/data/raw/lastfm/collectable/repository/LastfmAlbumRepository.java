package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends JpaRepository<LastfmAlbum, Long> {

    @Query("""
        SELECT al
        FROM
            album al
        JOIN
            al.artist ar
        WHERE   lower(al.name) = lower(:albumName)
            AND lower(ar.name) = lower(:artistName)
    """)
    List<LastfmAlbum> findByNameAndArtistName(@Param("albumName") String albumName, @Param("artistName") String artistName);

    List<LastfmAlbum> findAllByUrlIn(List<String> urls);

    List<LastfmAlbum> findAllByUrl(String url);

    List<LastfmAlbum> findAllByMbid(String mbid);

    List<LastfmAlbum> findAllByMbidIn(List<String> mbids);

    @Query(value = """
        SELECT  a
        FROM    album a
        LEFT JOIN FETCH a.artist
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR a.artist.id = :artistId)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM album_tag at 
                WHERE at.album.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmAlbum> findAlbumsWithoutApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    @Query(value = """
        SELECT  a
        FROM    album a
        LEFT JOIN FETCH a.artist
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR a.artist.id = :artistId)
            AND a.approvalStatus IN (:approvalStatuses)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM album_tag at 
                WHERE at.album.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmAlbum> findAlbumsWithApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    /**
     * A wrapper for findAlbums for correct collection parameters resolution.
     * This implementation avoids Hibernate bugs with:
     * 1. Null String parameters being recognized as bytea
     * 2. Empty collections handling
     * 3. Ensures null values are sorted last for numeric fields
     */
    default Page<LastfmAlbum> findAlbums(
        String search,
        Long minPlayCount,
        Long minListenersCount,
        Long artistId,
        List<ApprovalStatus> approvalStatuses,
        Long tagId,
        Pageable pageable
    ) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findAlbumsWithoutApprovalStatus(search, minPlayCount, minListenersCount, artistId, tagId, pageable);
        } else {
            return findAlbumsWithApprovalStatus(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId, pageable);
        }
    }

    /**
     * Find albums with missing playCount and listenersCount that haven't been processed by album.getInfo yet.
     * Excludes albums that are blacklisted.
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
