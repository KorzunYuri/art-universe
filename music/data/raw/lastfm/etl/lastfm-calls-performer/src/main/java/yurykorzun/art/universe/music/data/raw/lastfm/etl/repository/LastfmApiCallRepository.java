package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;

@Repository
public interface LastfmApiCallRepository extends BaseLastfmApiCallRepository {

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
}
