package yurykorzun.art.universe.music.data.raw.spotify.staging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyStagingIterationRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class StagingIterationServiceImpl implements StagingIterationService {

    private final SpotifyStagingIterationRepository repository;
    private final StagingWriter stagingWriter;
    private final long maxRecords;
    private final long maxOpenMinutes;

    public StagingIterationServiceImpl(
        SpotifyStagingIterationRepository repository,
        StagingWriter stagingWriter,
        @Value("${spotify.staging.iteration.max-records}") long maxRecords,
        @Value("${spotify.staging.iteration.max-open-minutes}") long maxOpenMinutes
    ) {
        this.repository = repository;
        this.stagingWriter = stagingWriter;
        this.maxRecords = maxRecords;
        this.maxOpenMinutes = maxOpenMinutes;
    }

    @Override
    @Transactional
    public StagingIteration getOrCreateOpenIteration() {
        return repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN)
            .orElseGet(this::createNewIteration);
    }

    @Override
    @Transactional
    public void seal(StagingIteration iteration) {
        iteration.setStatus(StagingIterationStatus.SEALED);
        iteration.setSealedAt(Instant.now());
        repository.save(iteration);
        log.info("Sealed staging iteration {} with {} records staged", iteration.getId(), iteration.getRecordsStaged());
    }

    @Override
    public boolean shouldSeal(StagingIteration iteration) {
        if (iteration.getRecordsStaged() == 0) {
            return false;
        }
        if (iteration.getRecordsStaged() >= maxRecords) {
            return true;
        }
        Instant threshold = iteration.getOpenedAt().plus(maxOpenMinutes, ChronoUnit.MINUTES);
        return Instant.now().isAfter(threshold);
    }

    @Override
    @Transactional
    public void incrementRecordsStaged(StagingIteration iteration, int count) {
        iteration.setRecordsStaged(iteration.getRecordsStaged() + count);
        repository.save(iteration);
    }

    private StagingIteration createNewIteration() {
        StagingIteration iteration = StagingIteration.builder().build();
        iteration = repository.save(iteration);
        stagingWriter.createTablesForIteration(iteration.getId());
        log.info("Created new staging iteration {}", iteration.getId());
        return iteration;
    }
}
