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
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.Instant;
import java.util.List;

@Repository
public interface LastfmApiCallRepository extends JpaRepository<LastfmApiCall, Long> {

    @Query(value = """
            WITH unexpired_calls AS (
                SELECT  *
                FROM    api_call
                WHERE   due_dttm > now()
                    AND status = :status
            ),
            type_stats AS (
                SELECT
                        type
                    ,   COUNT(*) AS type_count
                FROM unexpired_calls
                GROUP BY type
            ),
            total_count AS (
                SELECT SUM(type_count) AS total
                FROM type_stats
            ),
            distribution AS (
                SELECT
                        ts.type
                    ,   CEIL(ts.type_count * 1.0 / tc.total * :batchSize)::int AS target_count
                FROM
                    type_stats ts, 
                    total_count tc
            ),
            ranked_calls AS (
                SELECT
                    uc.*,
                    ROW_NUMBER() OVER ( PARTITION   BY uc.type 
                                        ORDER       BY uc.created_at) AS rn   -- TODO add priority field and select api calls by priority
                FROM unexpired_calls uc
            ),
            final_selection AS (
                SELECT  rc.*
                FROM    ranked_calls rc
                JOIN    distribution d 
                            ON rc.type = d.type
                WHERE   rc.rn <= d.target_count
            )
            SELECT  *
            FROM    final_selection
            LIMIT   :batchSize;
        """, nativeQuery = true)
    List<LastfmApiCall> findAllUnexpiredByStatus(@Param("status") int status, @Param("batchSize") int batchSize);

    default List<LastfmApiCall> findAllUnprocessedUnexpired(int batchSize) {
        return findAllUnexpiredByStatus(ApiCallStatus.PENDING.getCode(), batchSize);
    }

    default List<LastfmApiCall> findAllUnprocessedUnexpired() {
        return findAllUnprocessedUnexpired(LastfmConstants.HIBERNATE_BATCH_SIZE);
    }

    @Modifying
    @Query(value =
            "   UPDATE  api_call " +
            "   SET     status = :status " +
            "   WHERE   1=1 " +
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
