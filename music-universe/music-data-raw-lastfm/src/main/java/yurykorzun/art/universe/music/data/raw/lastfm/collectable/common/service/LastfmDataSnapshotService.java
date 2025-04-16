package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;

import java.util.List;

public interface LastfmDataSnapshotService {

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType);

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType, BaseLastfmEntity entity);

    void incCreatedCount(           long id             );
    void incCreatedCountByNumber(   long id,        int number );
    void incCreatedCount(           List<Long> ids);
    void incCreatedCountByNumber(   List<Long> ids, int number);

    void incCompletedCount(long id             );
    void incCompletedCountByNumber( long id, int number );

    void incParsedCount(            long id             );
    void incParsedCountByNumber(    long id, int number );

}
