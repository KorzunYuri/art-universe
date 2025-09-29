package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmArtistRepository extends BaseLastfmArtistRepository {

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
}
