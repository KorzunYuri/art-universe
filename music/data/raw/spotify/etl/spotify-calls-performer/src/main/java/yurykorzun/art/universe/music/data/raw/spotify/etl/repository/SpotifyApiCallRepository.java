package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

import java.util.List;

@Repository
public interface SpotifyApiCallRepository extends BaseSpotifyApiCallRepository {

    @Query(
        value = "SELECT * FROM api_call WHERE status IN (:statusCodes) AND due_dttm > NOW() ORDER BY priority DESC, id ASC LIMIT :batchSize",
        nativeQuery = true
    )
    List<SpotifyApiCall> findByStatusCodesAndNotExpired(
        @Param("statusCodes") List<Integer> statusCodes,
        @Param("batchSize") int batchSize
    );

    default List<SpotifyApiCall> findAllCreatedUnexpired() {
        return findByStatusCodesAndNotExpired(
            List.of(ApiCallStatus.CREATED.getCode(), ApiCallStatus.DUE_TO_RETRY.getCode()),
            SpotifyConstants.HIBERNATE_BATCH_SIZE
        );
    }

    @Query(
        value = "SELECT * FROM api_call WHERE status = :statusCode AND due_dttm > NOW() ORDER BY priority DESC, id ASC LIMIT :batchSize",
        nativeQuery = true
    )
    List<SpotifyApiCall> findByStatusAndNotExpired(
        @Param("statusCode") int statusCode,
        @Param("batchSize") int batchSize
    );

    @Query(
        value = "SELECT * FROM api_call WHERE status = :statusCode AND (kafka_produced IS NULL OR kafka_produced = false) ORDER BY id ASC LIMIT :batchSize",
        nativeQuery = true
    )
    List<SpotifyApiCall> findByStatusAndNotKafkaProduced(
        @Param("statusCode") int statusCode,
        @Param("batchSize") int batchSize
    );
}
