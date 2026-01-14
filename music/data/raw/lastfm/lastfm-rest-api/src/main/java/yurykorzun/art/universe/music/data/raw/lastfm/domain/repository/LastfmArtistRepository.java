package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;

import java.util.List;

@Repository
public interface LastfmArtistRepository extends BaseLastfmArtistRepository {

    @Query(value = """
        SELECT  a
        FROM    artist a
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM artist_tag at 
                WHERE at.artist.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmArtist> findArtistsWithoutApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    @Query(value = """
    SELECT  a
    FROM    artist a
    JOIN    artist_tag at ON a.id = at.artist.id
    WHERE   1=1
        AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
        AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
        AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
        AND at.tag.id = :tagId
    ORDER BY (at.usageCount / 10) DESC,
             COALESCE(a.listenersCount, 0) DESC,
             a.name ASC
    """)
    Page<LastfmArtist> findArtistsByTagWithoutApprovalStatus(
            @Nullable @Param("search")              String search,
            @Nullable @Param("minPlayCount")        Long minPlayCount,
            @Nullable @Param("minListenersCount")   Long minListenersCount,
            @Param("tagId")                         Long tagId,
            Pageable pageable);

    @Query(value = """
    SELECT  a
    FROM    artist a
    JOIN    artist_tag at ON a.id = at.artist.id
    WHERE   1=1
        AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
        AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
        AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
        AND a.approvalStatus IN (:approvalStatuses)
        AND at.tag.id = :tagId
    ORDER BY (at.usageCount / 10) DESC,
             COALESCE(a.listenersCount, 0) DESC,
             a.name ASC
    """)
    Page<LastfmArtist> findArtistsByTagWithApprovalStatus(
            @Nullable @Param("search")              String search,
            @Nullable @Param("minPlayCount")        Long minPlayCount,
            @Nullable @Param("minListenersCount")   Long minListenersCount,
            @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
            @Param("tagId")                         Long tagId,
            Pageable pageable);

    @Query(value = """
        SELECT  a
        FROM    artist a
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND a.approvalStatus IN (:approvalStatuses)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM artist_tag at 
                WHERE at.artist.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmArtist> findArtistsWithApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    /**
     * A wrapper for findArtists for correct collection parameters resolution.
     * In general, I faced a couple of bugs here:
     * <ul>
     *     <li><a href="https://stackoverflow.com/questions/77881433/org-postgresql-util-psqlexception-error-function-lowerbytea-does-not-exist">
     *         Hibernate recognizing null String as bytea</a>
     *          - fixed by changing the order of appearance of statements containing null string</li>
     *      <li><a href="https://github.com/abstratt/cloudfier/issues/107">Null parameters issue</a>
     *          - fixed by implementing different signatures, as you may see here</li>
     * </ul>
     */
    default Page<LastfmArtist> findArtists(
            String search,
            Long minPlayCount,
            Long minListenersCount,
            List<ApprovalStatus> approvalStatuses,
            Long tagId,
            Pageable pageable
    ) {
        if (tagId != null) {
            if (approvalStatuses == null || approvalStatuses.isEmpty()) {
                return findArtistsByTagWithoutApprovalStatus(search, minPlayCount, minListenersCount, tagId, pageable);
            } else {
                return findArtistsByTagWithApprovalStatus(search, minPlayCount, minListenersCount, approvalStatuses, tagId, pageable);
            }
        } else {
            if (approvalStatuses == null || approvalStatuses.isEmpty()) {
                return findArtistsWithoutApprovalStatus(search, minPlayCount, minListenersCount, tagId, pageable);
            } else {
                return findArtistsWithApprovalStatus(search, minPlayCount, minListenersCount, approvalStatuses, tagId, pageable);
            }
        }
    }
}
