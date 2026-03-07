package yurykorzun.art.universe.music.data.raw.spotify.task.call.generate;

import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.util.List;

/**
 * Provides entities that are candidates for new API call generation —
 * those without an active (non-expired) api_call of the given type.
 */
public interface SpotifyApiCallEntityService {

    <E extends BaseSpotifyEntity> List<E> findAllWithoutActiveCalls(Class<E> entityClass, SpotifyApiCallType callType);
}
