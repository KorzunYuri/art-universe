package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.*;

@Repository
public interface LastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {

    Optional<LastfmArtist> findByName(String name);

    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

    /**
     *  Returns artists for artist.getInfo method, in the following priority order:
     *  <ol>
     *      <li>approved artists</li>
     *      <li>top-artists for top-tags that don't have scrobbling stats yet</li>
     *      <li>similar artists for top-artists of top-tags that don't have scrobbling stats yet</li>
     *  </ol>
     */
    @Query(value = """
            WITH top_tags AS (
                SELECT
                        ah.entity_id as tag_id
                    ,   ah.int_value as tag_rank
                FROM
                    attribute_history ah
                JOIN
                    tag t
                        ON  1=1
                            AND ah.entity_id        = t.id
                            AND t.approval_status   = 2   -- approved
                WHERE   1=1
                    and ah.scope_entity_type  is null
                    and ah.scope_entity_id    is null
                    and ah.entity_type        = 4       -- tag
                    and ah.attribute_id       = 4       -- rank
                    and ah.valid_till         = '9999-12-31'
            )
            , top_artists AS (
                SELECT
                        top_tags.tag_id
                    ,   top_tags.tag_rank
                    ,   artist_rank.entity_id   as artist_id
                    ,   artist_rank.int_value   as artist_rank
                FROM
                    top_tags
                JOIN
                    attribute_history as artist_rank
                        ON      1=1
                            AND artist_rank.scope_entity_type   =   4       -- tag
                            AND artist_rank.scope_entity_id     =   top_tags.tag_id
                            AND artist_rank.entity_type         =   1       -- artist
                            AND artist_rank.attribute_id        =   4       -- rank
                            AND artist_rank.valid_till          =   '9999-12-31'
            )
            , approved_artists AS (
                SELECT
                        a.id    as id
                    ,   1       as priority_1
                    ,   0       as priority_2
                FROM    
                    artist a
                LEFT JOIN 
                    api_call ac
                        ON  1=1
                            AND ac.entity_type  = 1       -- artist
                            AND ac.entity_id    = a.id
                            AND ac.type         = 4       -- getInfo
                            AND ac.due_dttm     > now()
                WHERE   1=1
                    AND a.approval_status   = 2         -- approved
                    AND ac.id               IS NULL     -- no pending api_calls with artist.getInfo type
                LIMIT   :batchSize
            )
            , top_artists_no_info AS (
                SELECT  
                        a.id
                    ,   ta.artist_rank  as priority_1
                    ,   ta.tag_rank     as priority_2
                FROM    
                    top_artists ta
                JOIN    
                    artist a
                        ON  a.id = ta.artist_id
                LEFT JOIN 
                    api_call ac
                        ON  1=1
                            AND ac.entity_type  = 1       -- artist
                            AND ac.entity_id    = a.id
                            AND ac.type         = 4       -- getInfo
                            AND ac.due_dttm     > now()
                WHERE   1=1
                    AND (       a.listeners_count   IS NULL
                            OR  a.play_count        IS NULL )   -- data from artist.getInfo is missing
                    AND ac.id                       IS NULL     -- no pending api_calls with artist.getInfo type
                LIMIT :batchSize
            )
            , similar_artists AS (
                SELECT
                        a.id
                    ,   ta.artist_rank  + 1000000  as priority_1
                    ,   ta.tag_rank     + 1000000  as priority_2
                FROM
                    top_artists ta
                JOIN
                    artist_artist rel
                        ON      1=1
                            AND rel.source_artist_id     =   ta.artist_id
                JOIN
                    artist a
                        ON      1=1
                            AND a.id = rel.target_artist_id
                LEFT JOIN 
                    api_call ac
                        ON  1=1
                            AND ac.entity_type  = 1       -- artist
                            AND ac.entity_id    = a.id
                            AND ac.type         = 4       -- getInfo
                            AND ac.due_dttm     > now()
                WHERE   1=1
                    AND (       a.listeners_count   IS NULL
                            OR  a.play_count        IS NULL )   -- data from artist.getInfo is missing
                    AND ac.id                       IS NULL     -- no pending api_calls with artist.getInfo type
                LIMIT :batchSize
            )
            , not_approved_artists_no_info AS (
                SELECT
                        a.id    as id
                    ,   100000000 + a.id as priority_1
                    ,   100000000 + a.id as priority_2
                FROM    
                    artist a
                LEFT JOIN 
                    api_call ac
                        ON  1=1
                            AND ac.entity_type  = 1       -- artist
                            AND ac.entity_id    = a.id
                            AND ac.type         = 4       -- getInfo
                            AND ac.due_dttm     > now()
                WHERE   1=1
                    AND a.approval_status   = 1         -- pending approval
                    AND a.listeners_count   IS NULL
                    AND ac.id               IS NULL     -- no pending api_calls with artist.getInfo type
                LIMIT   :batchSize
            )
            , union_artists AS (
                SELECT * FROM approved_artists
                UNION
                SELECT * FROM top_artists_no_info
                UNION
                SELECT * FROM similar_artists
                UNION
                SELECT * FROM not_approved_artists_no_info
            )
            SELECT
                a.*
            FROM
                union_artists ua
            JOIN 
                artist a
                    ON  a.id = ua.id
            ORDER BY ua.priority_1, ua.priority_2
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
        """)
    Page<LastfmArtist> findArtistsWithoutApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        Pageable pageable);

    @Query(value = """
        SELECT  a
        FROM    artist a
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND a.approvalStatus IN (:approvalStatuses)
        """)
    Page<LastfmArtist> findArtistsWithApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
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
        Pageable pageable
    ) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findArtistsWithoutApprovalStatus(search, minPlayCount, minListenersCount, pageable);
        } else {
            return findArtistsWithApprovalStatus(search, minPlayCount, minListenersCount, approvalStatuses, pageable);
        }
    }
}
