package yurykorzun.art.universe.music.data.raw.lastfm.etl.service;

import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;

import java.util.List;

public interface LastfmDataSnapshotService {

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType);

    LastfmDataSnapshot getOrCreateSnapshotFor(LastfmApiCallType apiCallType, BaseLastfmEntity entity);

    void incCreatedCount(           long id             );
    void incCreatedCountByNumber(   long id,        int number );
    void incCreatedCount(           List<Long> ids);
    void incCreatedCountByNumber(   List<Long> ids, int number);

}
