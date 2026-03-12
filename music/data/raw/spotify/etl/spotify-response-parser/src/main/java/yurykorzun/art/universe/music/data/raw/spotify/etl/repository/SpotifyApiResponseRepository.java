package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiResponse;

import java.util.List;

@Repository
public interface SpotifyApiResponseRepository extends BaseSpotifyApiResponseRepository {

    @Query(
        value = "SELECT * FROM api_response WHERE status = :statusCode ORDER BY id ASC LIMIT :batchSize",
        nativeQuery = true
    )
    List<SpotifyApiResponse> findByStatusCodeOrderByIdAsc(
        @Param("statusCode") int statusCode,
        @Param("batchSize") int batchSize
    );

    default List<SpotifyApiResponse> findAllPending() {
        return findByStatusCodeOrderByIdAsc(ApiResponseStatus.PENDING.getCode(), SpotifyConstants.HIBERNATE_BATCH_SIZE);
    }
}
