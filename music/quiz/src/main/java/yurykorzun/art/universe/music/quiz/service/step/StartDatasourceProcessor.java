package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

@Component
public class StartDatasourceProcessor extends BaseGenerationStepProcessor {

    public StartDatasourceProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(GenerationStepType.START_DATASOURCE, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "start_datasource";
    }
}
