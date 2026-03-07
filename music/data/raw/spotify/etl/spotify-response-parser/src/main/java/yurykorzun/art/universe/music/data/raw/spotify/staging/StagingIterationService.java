package yurykorzun.art.universe.music.data.raw.spotify.staging;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.StagingIteration;

public interface StagingIterationService {

    StagingIteration getOrCreateOpenIteration();

    void seal(StagingIteration iteration);

    boolean shouldSeal(StagingIteration iteration);

    void incrementRecordsStaged(StagingIteration iteration, int count);
}
