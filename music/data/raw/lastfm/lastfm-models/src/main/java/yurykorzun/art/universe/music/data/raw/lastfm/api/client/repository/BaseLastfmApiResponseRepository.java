package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;

import java.util.List;

import static yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants.HIBERNATE_BATCH_SIZE;

@Repository
public interface BaseLastfmApiResponseRepository extends JpaRepository<LastfmApiResponse, Long> {

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
