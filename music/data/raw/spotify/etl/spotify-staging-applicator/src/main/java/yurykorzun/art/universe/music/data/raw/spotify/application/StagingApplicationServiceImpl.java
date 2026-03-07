package yurykorzun.art.universe.music.data.raw.spotify.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.util.List;

@Service
@Slf4j
public class StagingApplicationServiceImpl implements StagingApplicationService {

    private final SpotifyStagingIterationRepository iterationRepository;
    private final StagingIterationApplicator iterationApplicator;

    public StagingApplicationServiceImpl(
        SpotifyStagingIterationRepository iterationRepository,
        StagingIterationApplicator iterationApplicator
    ) {
        this.iterationRepository = iterationRepository;
        this.iterationApplicator = iterationApplicator;
    }

    @Override
    public void applySealedIterations() {
        List<StagingIteration> sealed = iterationRepository.findAllByStatusOrderByOpenedAtAsc(StagingIterationStatus.SEALED);
        if (sealed.isEmpty()) {
            return;
        }
        log.info("Found {} sealed staging iterations to apply", sealed.size());
        for (StagingIteration iteration : sealed) {
            iterationApplicator.apply(iteration);
        }
    }
}
