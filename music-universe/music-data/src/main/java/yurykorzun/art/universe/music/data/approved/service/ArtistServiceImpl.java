package yurykorzun.art.universe.music.data.approved.service;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.ArtistRepository;

import java.util.List;

@Service
public class ArtistServiceImpl implements ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistBindingRepository bindingsRepository;

    public ArtistServiceImpl(
        ArtistRepository artistRepository,
        ArtistBindingRepository bindingsRepository
    ) {
        this.artistRepository = artistRepository;
        this.bindingsRepository = bindingsRepository;
    }

    @Override
    public List<BoundEntityProjection> findBoundArtists(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundArtistsForDataSource(dataSource, externalIds);
    }
}
