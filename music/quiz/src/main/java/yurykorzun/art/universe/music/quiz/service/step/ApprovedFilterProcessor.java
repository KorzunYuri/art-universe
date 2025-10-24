package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

@Component
public class ApprovedFilterProcessor extends BaseGenerationStepProcessor {

    public ApprovedFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(GenerationStepType.APPROVED_FILTER, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "approved_filter";
    }
}
