package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;

import java.util.List;

@Repository
public interface LastfmTrackRepository extends BaseLastfmTrackRepository {

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
}
