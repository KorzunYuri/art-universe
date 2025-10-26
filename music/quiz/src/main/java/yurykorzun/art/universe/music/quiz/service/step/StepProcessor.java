package yurykorzun.art.universe.music.quiz.service.step;

import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;

public interface StepProcessor {
    
    StepType getStepType();

    /**
     * Validate the configuration against current version of processor logic.
     * If possible, migrate configuration to the actual version.
     *
     * @return configuration compatible with the actual version.
     *
     * @throws IllegalArgumentException if configuration is invalid or unable to migrate to the actual version.
     */
    String verifyConfigurationIsActual(String cfgData);

    String getPreview(Step step);

    /**
     * Take table containing (artist_id, track_id, [chance]) fields, process an operation on them and create a new table with the results.
     * @param step step to execute
     * @param inputTableName table containing input data, in format (artist_id, track_id, [chance])
     * @param stepTableNameBase base for output and auxiliary tables' names
     * @param stepRun step run (needed to extract some metadata)
     * @return object containing information about the execution
     */
    StepRunResult processStep(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun);

    /**
     * Calculate step stats on successfully executed step run.
     */
    StepRunStats getResultStats(StepRun stepRun);
}
