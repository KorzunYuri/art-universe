package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;

import java.time.Instant;
import java.util.List;

@Repository
public interface LastfmApiCallRepository extends JpaRepository<LastfmApiCall, Long> {

    @Query(value = "SELECT * " +
            "FROM api_call " +
            "WHERE  1=1 " +
            "   AND status = :status " +
            "   AND due_dttm > NOW()" +
            "LIMIT :batchSize",
            nativeQuery = true)
    List<LastfmApiCall> findAllUnexpiredByStatus(@Param("status") int status, @Param("batchSize") int batchSize);

    default List<LastfmApiCall> findAllUnprocessedUnexpired(int batchSize) {
        return findAllUnexpiredByStatus(ApiCallStatus.PENDING.getCode(), batchSize);
    }

    default List<LastfmApiCall> findAllUnprocessedUnexpired() {
        return findAllUnprocessedUnexpired(50);
    }

    @Modifying
    @Query(value =
            "   UPDATE api_call " +
            "   SET status = :status " +
            "   WHERE  1=1 " +
            "       AND type = :type " +
            "       AND due_dttm < NOW()",
        nativeQuery = true)
    void expireOutdatedApiCallsByType(@Param("status") int status, @Param("type") int type);

    default void expireOutdatedApiCallsByType(LastfmApiCallType type) {
        expireOutdatedApiCallsByType(ApiCallStatus.PENDING.getCode(), type.getCode());
    }

    /**
     * Find all api calls of type {@param type} that haven't expired yet, of whatever status
     */
    default List<LastfmApiCall> findAllUnexpiredByType(@NonNull ApiCallType type) {
        return findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(type, Instant.now());
    }

    List<LastfmApiCall> findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(@NonNull ApiCallType type, @NonNull Instant dueDttm);
}
