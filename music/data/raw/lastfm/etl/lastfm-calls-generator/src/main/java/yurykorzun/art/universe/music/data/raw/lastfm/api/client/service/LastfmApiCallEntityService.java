package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;


import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

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
