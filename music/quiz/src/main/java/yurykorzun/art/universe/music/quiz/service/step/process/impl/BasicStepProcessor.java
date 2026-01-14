package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Setter;
import yurykorzun.art.universe.common.persistence.DatabaseUtils;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.stats.BasicStepStats;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessor;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

public abstract class BasicStepProcessor implements StepProcessor {

    @PersistenceContext
    @Setter // for test purposes
    protected EntityManager entityManager;

    public BasicStepProcessor(StepProcessorRegistry registry) {
        registry.register(this);
    }

    @Override
    public String getPreview(Step step) {
        return "{}";
    }

    // TODO pass the version as a separate parameter
    @Override
    public String verifyConfigurationIsActual(String cfgData) {
        if (!isActualVersion(cfgData)) {
            return migrateConfiguration(cfgData);
        }
        validateConfiguration(cfgData);
        return cfgData;
    }

    protected void validateConfiguration(String cfgData) {
        // override for steps that have parameters
    }

    protected void validateInputTable(String inputTableName) {
        DatabaseUtils.validateObjectName(inputTableName);
    }

    protected void validateStepTableNameBase(String stepTableNameBase) {
        DatabaseUtils.validateObjectName(stepTableNameBase);
    }

    protected boolean isActualVersion(String cfgData) {
        return true;
    }

    protected String migrateConfiguration(String cfgData) {
        return cfgData;
    }

    @Override
    public final StepRunResult processStep(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
        validateConfiguration(step.getCfgData());
        validateStepTableNameBase(stepTableNameBase);
        validateInputTable(inputTableName);
        return executeStepLogic(step, inputTableName, stepTableNameBase, stepRun);
    }

    protected abstract StepRunResult executeStepLogic(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun);

    /**
     * Basic implementation of stats calculation. The return type can be extended by specific implementations.
     */
    @Override
    public StepRunStats getResultStats(StepRun stepRun) {
        BasicStepStats stats = new BasicStepStats();

        String inputTableName = stepRun.getInputTableName();
        String outputTableName = stepRun.getResultTableName();

        // Check table existence
        boolean inputExists = inputTableName != null && DatabaseUtils.tableExists(entityManager, inputTableName);
        boolean outputExists = outputTableName != null && DatabaseUtils.tableExists(entityManager, outputTableName);

        stats.setInputRecords(inputExists ? getTrackCount(inputTableName) : 0L);
        stats.setInputArtists(inputExists ? getArtistCount(inputTableName) : 0L);
        stats.setOutputRecords(outputExists ? getTrackCount(outputTableName) : 0L);
        stats.setOutputArtists(outputExists ? getArtistCount(outputTableName) : 0L);
        stats.setFilteredRecords(stats.getInputRecords() - stats.getOutputRecords());
        stats.setFilteredArtists(stats.getInputArtists() - stats.getOutputArtists());

        return stats;
    }

    protected Long getTrackCount(String tableName) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(*) FROM " + tableName)
            .getSingleResult()).longValue();
    }

    protected Long getArtistCount(String tableName) {
        return ((Number) entityManager.createNativeQuery("SELECT COUNT(DISTINCT primary_artist_id) FROM " + tableName)
            .getSingleResult()).longValue();
    }
}
