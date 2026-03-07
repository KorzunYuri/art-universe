package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;

import java.time.Instant;
import java.util.List;

@Repository
public interface SpotifyStagingIterationRepository extends BaseSpotifyStagingIterationRepository {

    List<StagingIteration> findAllByStatusOrderByOpenedAtAsc(StagingIterationStatus status);

    List<StagingIteration> findAllByStatusInAndSealedAtBefore(List<StagingIterationStatus> statuses, Instant before);
}
