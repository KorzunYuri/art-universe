package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;

import java.util.List;

import static yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants.HIBERNATE_BATCH_SIZE;

@Repository
public interface LastfmApiResponseRepository extends BaseLastfmApiResponseRepository {

    @Query(
        value = "SELECT * FROM api_response " +
            "WHERE status = :statusCode " +
            "FOR UPDATE SKIP LOCKED " +
            "LIMIT :batchSize",
        nativeQuery = true)
    List<LastfmApiResponse> findAllByStatus(@Param("statusCode") int statusCode, @Param("batchSize") int batchSize);

    default List<LastfmApiResponse> findAllByStatus(ApiResponseStatus status) {
        return findAllByStatus(status.getCode(), HIBERNATE_BATCH_SIZE);
    };

    default List<LastfmApiResponse> findAllPending() {
        return findAllByStatus(ApiResponseStatus.PENDING);
    }
}
