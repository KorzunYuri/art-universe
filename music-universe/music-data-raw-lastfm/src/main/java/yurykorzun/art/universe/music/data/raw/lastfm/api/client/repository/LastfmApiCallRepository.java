package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

import java.util.Collection;

@Repository
public interface LastfmApiCallRepository extends JpaRepository<LastfmApiCall, Long> {

    // TODO add limit for ApiCall findAll method
    Collection<LastfmApiCall> findAllByTypeAndStatusOrderByDueDttmAsc(@NonNull ApiCallType type, ApiCallStatus status);

    default Collection<LastfmApiCall> findAllUnprocessedByType(@NonNull ApiCallType type) {
        return findAllByTypeAndStatusOrderByDueDttmAsc(type, ApiCallStatus.CREATED);
    }

    Collection<LastfmApiCall> findAllByStatus(@NonNull ApiCallStatus status);

    default Collection<LastfmApiCall> findAllUnprocessed() {
        return findAllByStatus(ApiCallStatus.CREATED);
    };
}
