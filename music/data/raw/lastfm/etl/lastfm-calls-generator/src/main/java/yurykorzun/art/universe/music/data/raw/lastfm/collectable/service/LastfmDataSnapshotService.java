package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;

import java.util.List;

public interface LastfmDataSnapshotService {

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType);

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType, BaseLastfmEntity entity);

    void incCreatedCount(           long id             );
    void incCreatedCountByNumber(   long id,        int number );
    void incCreatedCount(           List<Long> ids);
    void incCreatedCountByNumber(   List<Long> ids, int number);

}
