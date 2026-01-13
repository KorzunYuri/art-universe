package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import lombok.NonNull;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;

import java.time.Instant;
import java.util.List;

@Repository
public interface LastfmApiCallRepository extends BaseLastfmApiCallRepository {

    /**
     * Find all api calls of type {@param type} that haven't expired yet, of whatever status
     */
    default List<LastfmApiCall> findAllUnexpiredByType(@NonNull ApiCallType type) {
        return findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(type, Instant.now());
    }

    List<LastfmApiCall> findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(@NonNull ApiCallType type, @NonNull Instant dueDttm);
}
