package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

public interface TrackService {

    List<BoundEntityProjection> findBoundTracks(DataSource dataSource, List<Long> externalIds);

}
