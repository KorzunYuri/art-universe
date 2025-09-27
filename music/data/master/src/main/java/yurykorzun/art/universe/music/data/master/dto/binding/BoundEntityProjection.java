package yurykorzun.art.universe.music.data.master.dto.binding;

import yurykorzun.art.universe.music.data.master.entity.DataSource;

public interface BoundEntityProjection {
    Long        getExternalId();
    DataSource  getDataSource();
    Long        getMasterId();
    String      getMasterName();
}