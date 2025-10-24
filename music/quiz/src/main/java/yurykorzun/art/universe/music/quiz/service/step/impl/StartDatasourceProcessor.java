package yurykorzun.art.universe.music.quiz.service.step.impl;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.config.StartDatasourceStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class StartDatasourceProcessor extends BaseGenerationStepProcessor {

    public StartDatasourceProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(StepType.START_DATASOURCE, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "start";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        return StartDatasourceStepConfig.DEFAULT_DATASOURCE;
    }
}
