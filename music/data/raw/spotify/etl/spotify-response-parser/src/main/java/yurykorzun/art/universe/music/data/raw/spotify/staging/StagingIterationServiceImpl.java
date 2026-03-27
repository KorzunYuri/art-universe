package yurykorzun.art.universe.music.data.raw.spotify.staging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotifyEventPublisher;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyParserProperty;
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
    private final ConfigPropertyHolder configPropertyHolder;
    private final PgNotifyEventPublisher pgNotifyEventPublisher;

    public StagingIterationServiceImpl(
        SpotifyStagingIterationRepository repository,
        StagingWriter stagingWriter,
        ConfigPropertyHolder configPropertyHolder,
        PgNotifyEventPublisher pgNotifyEventPublisher
    ) {
        this.repository = repository;
        this.stagingWriter = stagingWriter;
        this.configPropertyHolder = configPropertyHolder;
        this.pgNotifyEventPublisher = pgNotifyEventPublisher;
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
        pgNotifyEventPublisher.notifyAfterCommit(SpotifyConstants.NOTIFY_ITERATIONS_SEALED);
    }

    @Override
    public boolean shouldSeal(StagingIteration iteration) {
        if (iteration.getRecordsStaged() == 0) {
            return false;
        }
        long maxRecords = configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_RECORDS);
        if (iteration.getRecordsStaged() >= maxRecords) {
            return true;
        }
        long maxOpenMinutes = configPropertyHolder.getInt(SpotifyParserProperty.STAGING_ITERATION_MAX_OPEN_MINUTES);
        Instant threshold = iteration.getOpenedAt().plus(maxOpenMinutes, ChronoUnit.MINUTES);
        return Instant.now().isAfter(threshold);
    }

    @Override
    @Transactional
    public void incrementRecordsStaged(StagingIteration iteration, int count) {
        iteration.setRecordsStaged(iteration.getRecordsStaged() + count);
        repository.save(iteration);
    }

    @Override
    @Transactional
    public void sealIfExpired() {
        repository.findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus.OPEN)
            .filter(this::shouldSeal)
            .ifPresent(this::seal);
    }

    private StagingIteration createNewIteration() {
        StagingIteration iteration = StagingIteration.builder().build();
        iteration = repository.save(iteration);
        stagingWriter.createTablesForIteration(iteration.getId());
        log.info("Created new staging iteration {}", iteration.getId());
        return iteration;
    }
}
