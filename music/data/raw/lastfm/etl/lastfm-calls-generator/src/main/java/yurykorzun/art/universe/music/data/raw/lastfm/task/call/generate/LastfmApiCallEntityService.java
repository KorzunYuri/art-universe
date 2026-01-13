package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;


import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

/**
 * Base interface for services that provide entities that become scopes for API requests
 */
public interface LastfmApiCallEntityService {

    <E extends BaseLastfmEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType, LastfmApiCallEntityQueryConfig config);

    default <E extends BaseLastfmEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType) {
        return findAllUnprocessed(entityType, apiCallType, LastfmApiCallEntityQueryConfig.builder().build());
    }

}
