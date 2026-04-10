package yurykorzun.art.universe.music.data.master.dto.binding;

import yurykorzun.art.universe.music.data.master.model.DataSource;

public interface BoundEntityProjection {
    Long        getExternalId();
    DataSource  getDataSource();
    Long        getMasterId();
    String      getMasterName();
    /** The master entity's primary artist ID. Null for non-artist-related entities. */
    Long        getMasterPrimaryArtistId();
}