package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtistSearchRequest;

import java.util.List;

@Repository
public interface LastfmArtistSearchRequestRepository extends BaseLastfmArtistSearchRequestRepository {

    @Query(value = """
            SELECT  r.*
            FROM    artist_search r
            WHERE   r.is_processed = false
            ORDER BY r.created_at ASC
            LIMIT   :batchLimit
        """, nativeQuery = true)
    List<LastfmArtistSearchRequest> findUnprocessed(@Param("batchLimit") int batchLimit);
}
