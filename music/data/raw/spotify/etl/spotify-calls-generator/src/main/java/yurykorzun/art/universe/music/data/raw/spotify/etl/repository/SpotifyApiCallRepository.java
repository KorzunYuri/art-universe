package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpotifyApiCallRepository extends BaseSpotifyApiCallRepository {

    List<SpotifyApiCall> findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(SpotifyApiCallType type, Instant dueDttm);

    default List<SpotifyApiCall> findAllUnexpiredByType(SpotifyApiCallType type) {
        return findAllByTypeAndDueDttmAfterOrderByDueDttmAsc(type, Instant.now());
    }
}
