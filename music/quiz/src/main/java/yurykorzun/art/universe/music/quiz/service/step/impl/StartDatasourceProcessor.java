package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class StartDatasourceProcessor extends BaseGenerationStepProcessor {

    @Autowired
    private ObjectMapper objectMapper;

    public StartDatasourceProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(StepType.START_DATASOURCE, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "start";
    }

    @Override
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            StartDatasourceStepConfig config = objectMapper.readValue(step.getCfgData(), StartDatasourceStepConfig.class);
            return StepRunResult.builder()
                .outputTableName(config.getDatasource())
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }
}
