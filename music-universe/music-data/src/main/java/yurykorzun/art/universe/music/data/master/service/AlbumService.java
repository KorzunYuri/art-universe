package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;

public interface AlbumService {

    List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds);

}
