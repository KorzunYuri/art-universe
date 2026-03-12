package yurykorzun.art.universe.music.data.raw.spotify.etl.repository;

import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIterationStatus;

import java.util.Optional;

@Repository
public interface SpotifyStagingIterationRepository extends BaseSpotifyStagingIterationRepository {

    Optional<StagingIteration> findFirstByStatusOrderByOpenedAtAsc(StagingIterationStatus status);
}
