package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

@Component
public class FinalSelectionProcessor extends BaseGenerationStepProcessor {

    public FinalSelectionProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(GenerationStepType.FINAL_SELECTION, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "final_selection";
    }
}
