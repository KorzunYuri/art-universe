package yurykorzun.art.universe.music.data.approved.dto;

import yurykorzun.art.universe.music.data.approved.entity.DataSource;

public interface BoundEntityProjection {
    Long        getExternalId();
    DataSource  getDataSource();
    Long        getReferenceId();
    String      getReferenceName();
}