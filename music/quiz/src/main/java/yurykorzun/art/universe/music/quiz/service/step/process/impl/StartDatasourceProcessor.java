package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

@Component
public class StartDatasourceProcessor extends BasicStepProcessor {

    private final ObjectMapper objectMapper;

    public StartDatasourceProcessor(StepProcessorRegistry registry, ObjectMapper objectMapper) {
        super(registry);
        this.objectMapper = objectMapper;
    }

    @Override
    public StepType getStepType() {
        return StepType.START_DATASOURCE;
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }

    private StartDatasourceStepConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, StartDatasourceStepConfig.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }

    @Override
    protected void validateInputTable(String inputTableName) {
        // this step doesn't require input table
    }

    @Override
    protected StepRunResult executeStepLogic(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
        try {
            StartDatasourceStepConfig config = parseConfig(step.getCfgData());
            return StepRunResult.builder()
                .outputTableName(config.getDatasource())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }
}
