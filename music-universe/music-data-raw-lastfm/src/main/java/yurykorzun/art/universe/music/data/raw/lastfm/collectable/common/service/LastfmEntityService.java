package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service;


import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.List;

public interface LastfmEntityService {
    <E extends BaseLastfmEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType);
    <E extends BaseLastfmEntity> List<E> findAllUnprocessed(LastfmEntityType entityType, LastfmApiCallType apiCallType, LastfmEntityQueryConfig config);
}
