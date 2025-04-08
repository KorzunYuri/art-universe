package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

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
                        entity_id as tag_id
                    ,   int_value as tag_rank
                FROM
                    attribute_history
                WHERE   1=1
                    and scope_entity_type  is null
                    and scope_entity_id    is null
                    and entity_type        = 4      -- tag
                    and attribute_id       = 4      -- rank
                    and valid_till         = '9999-12-31'
                LIMIT :batchSize
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
                LIMIT :batchSize * 10
            )
            , approved_artists AS (
                SELECT  a.id,
                        1 as priority_1,
                        0 as priority_2
                FROM    artist a
                WHERE   a.approval_status = 2   -- approved
                LIMIT   :batchSize
            )
            , top_artists_no_info AS (
                SELECT  a.id,
                        ta.artist_rank  as priority_1,
                        ta.tag_rank     as priority_2
                FROM    top_artists ta
                JOIN    artist a
                    ON  a.id = ta.artist_id
                WHERE   1=1
                    AND (       a.listeners_count   IS NULL
                            OR  a.play_count        IS NULL )
            )
            , similar_artists AS (
                SELECT
                    a.id,
                    ta.artist_rank  + 1000000  as priority_1,
                    ta.tag_rank     + 1000000  as priority_2
                FROM
                    top_artists ta
                JOIN
                        entity_relation rel
                    ON      1=1
                        AND rel.scope_entity_type   =   1       -- artist
                        AND rel.scope_entity_id     =   ta.artist_id
                        AND rel.entity_type         =   1       -- artist
                        JOIN
                    artist a
                    ON      1=1
                        and a.id = rel.entity_id
                WHERE   1=1
                    AND (       a.listeners_count   IS NULL
                            OR  a.play_count        IS NULL )
            )
            , union_artists AS (
                SELECT * FROM approved_artists
                UNION ALL
                SELECT * FROM top_artists_no_info
                UNION ALL
                SELECT * FROM similar_artists
            )
            SELECT
                a.*
            FROM
                union_artists ua
            JOIN artist a
                ON  a.id = ua.id
            LEFT JOIN api_call ac
                ON  1=1
                    AND ac.entity_type  = 1       -- artist
                    AND ac.entity_id    = a.id
                    AND ac.type         = 4       -- getInfo
                    AND ac.due_dttm     > now()
            WHERE ac.id IS NULL
            ORDER BY ua.priority_1, ua.priority_2
            LIMIT :batchSize
        """,
        nativeQuery = true)
    List<LastfmArtist> findAllToGetInfoFor(@Param("batchSize") int limit);

    default List<LastfmArtist> findAllToGetInfoFor() {
        return findAllToGetInfoFor(LastfmConstants.HIBERNATE_BATCH_SIZE);
    }

}
