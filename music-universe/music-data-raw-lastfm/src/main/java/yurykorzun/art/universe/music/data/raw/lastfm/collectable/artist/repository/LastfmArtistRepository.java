package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.*;
import java.util.function.Function;

@Repository
public interface LastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {

    Optional<LastfmArtist> findByName(String name);

    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

    List<LastfmArtist> findAllByUrlIn(List<String> urls);

    /**
     * Returns artists that need artist.getInfo processing.
     * Prioritizes approved artists first, then by popularity, then by creation date (older first).
     * Excludes artists that are blacklisted or have pending API calls.
     * Deduplicates by MBID, preferring approved status and higher popularity.
     */
    @Query(value = """
        SELECT DISTINCT ON (COALESCE(a.mbid, 'id_' || a.id::text))
            a.id,
            a.name, 
            a.is_primary, 
            a.mbid, 
            a.url,
            a.listeners_count, 
            a.play_count, 
            a.approval_status, 
            a.api_call_id, 
            a.created_at, 
            a.updated_at
        FROM artist a
        WHERE (a.listeners_count IS NULL OR a.play_count IS NULL)
            AND a.approval_status IN (1, 2)  -- PENDING, APPROVED
            AND NOT EXISTS (
                SELECT 1
                FROM api_call ac
                WHERE ac.entity_type = 1       -- artist
                  AND ac.entity_id = a.id
                  AND ac.type = 4              -- getInfo
                  AND ac.due_dttm > NOW()
            )
            AND NOT EXISTS (
                SELECT 1
                FROM blacklist_entity_url bl
                WHERE bl.entity_type = 1        -- ARTIST
                  AND bl.url = a.url
            )
        ORDER BY 
            COALESCE(a.mbid, 'id_' || a.id::text),  -- group by MBID for deduplication
            a.approval_status DESC,           -- approved first (2 > 1)
            COALESCE(a.listeners_count, 0) DESC,  -- more popular first
            a.created_at ASC,                 -- older first
            a.id ASC                          -- stable sort
        LIMIT :batchSize
        """,
        nativeQuery = true)
    List<LastfmArtist> findAllToGetInfoFor(@Param("batchSize") int limit);

    default List<LastfmArtist> findAllToGetInfoFor() {
        return findAllToGetInfoFor(LastfmConstants.HIBERNATE_BATCH_SIZE);
    }

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
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findArtistsWithoutApprovalStatus(search, minPlayCount, minListenersCount, tagId, pageable);
        } else {
            return findArtistsWithApprovalStatus(search, minPlayCount, minListenersCount, approvalStatuses, tagId, pageable);
        }
    }

    Function<List<String>, List<? extends BaseLastfmEntity>> findAllByUrlIn(Collection<String> urls, Sort sort, Limit limit);
}
