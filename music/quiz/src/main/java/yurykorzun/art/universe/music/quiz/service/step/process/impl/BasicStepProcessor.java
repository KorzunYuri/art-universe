package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.Setter;
import yurykorzun.art.universe.common.persistence.util.DatabaseUtils;
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

    protected boolean isActualVersion(String cfgData) {
        return true;
    }

    protected String migrateConfiguration(String cfgData) {
        return cfgData;
    }

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

        if (!inputExists && !outputExists) {
            // Both tables don't exist - set all to 0
            stats.setInputRecords(0L);
            stats.setInputArtists(0L);
            stats.setFilteredRecords(0L);
            stats.setFilteredArtists(0L);
            stats.setOutputRecords(0L);
            stats.setOutputArtists(0L);
        } else if (!inputExists) {
            // Input doesn't exist (START_DATASOURCE case) - copy output stats to input stats
            Long outputRecords = outputExists ? getTrackCount(outputTableName) : 0L;
            Long outputArtists = outputExists ? getArtistCount(outputTableName) : 0L;

            stats.setInputRecords(outputRecords);
            stats.setInputArtists(outputArtists);
            stats.setFilteredRecords(0L);
            stats.setFilteredArtists(0L);
            stats.setOutputRecords(outputRecords);
            stats.setOutputArtists(outputArtists);
        } else if (!outputExists) {
            // Output doesn't exist - set output stats to 0
            Long inputRecords = getTrackCount(inputTableName);
            Long inputArtists = getArtistCount(inputTableName);

            stats.setInputRecords(inputRecords);
            stats.setInputArtists(inputArtists);
            stats.setFilteredRecords(inputRecords);
            stats.setFilteredArtists(inputArtists);
            stats.setOutputRecords(0L);
            stats.setOutputArtists(0L);
        } else {
            // Both tables exist - normal case
            Long inputRecords = getTrackCount(inputTableName);
            Long inputArtists = getArtistCount(inputTableName);
            Long outputRecords = getTrackCount(outputTableName);
            Long outputArtists = getArtistCount(outputTableName);

            stats.setInputRecords(inputRecords);
            stats.setInputArtists(inputArtists);
            stats.setFilteredRecords(inputRecords - outputRecords);
            stats.setFilteredArtists(inputArtists - outputArtists);
            stats.setOutputRecords(outputRecords);
            stats.setOutputArtists(outputArtists);
        }

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
