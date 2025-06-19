package yurykorzun.art.universe.music.data.approved.service;

import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.TrackRepository;

import java.util.List;

@Service
public class TrackServiceImpl implements TrackService {

    private final TrackRepository trackRepository;
    private final TrackBindingRepository bindingsRepository;

    public TrackServiceImpl(
        TrackRepository trackRepository,
        TrackBindingRepository bindingsRepository
    ) {
        this.trackRepository = trackRepository;
        this.bindingsRepository = bindingsRepository;
    }

    @Override
    public List<BoundEntityProjection> findBoundTracks(DataSource dataSource, List<Long> externalIds) {
        return bindingsRepository.findBoundTracksForDataSource(dataSource, externalIds);
    }
}
