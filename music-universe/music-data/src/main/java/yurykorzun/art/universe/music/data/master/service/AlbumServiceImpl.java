package yurykorzun.art.universe.music.data.master.service;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;

import java.util.List;

@Service
public class AlbumServiceImpl implements AlbumService {

    private final AlbumRepository albumRepository;
    private final AlbumBindingRepository bindingsRepository;

    public AlbumServiceImpl(
        AlbumRepository albumRepository,
        AlbumBindingRepository bindingsRepository
    ) {
        this.albumRepository = albumRepository;
        this.bindingsRepository = bindingsRepository;
    }

    @Override
    public List<BoundEntityProjection> findBoundAlbums(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundAlbumsForDataSource(dataSource, externalIds);
    }
}
