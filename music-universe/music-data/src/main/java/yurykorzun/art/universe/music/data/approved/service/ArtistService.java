package yurykorzun.art.universe.music.data.approved.service;

import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;

import java.util.List;

public interface ArtistService {

    List<BoundEntityProjection> findBoundArtists(DataSource dataSource, List<Long> externalIds);

}
