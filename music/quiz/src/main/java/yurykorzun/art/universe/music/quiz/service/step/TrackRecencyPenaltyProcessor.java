package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

@Component
public class TrackRecencyPenaltyProcessor extends BaseGenerationStepProcessor {

    public TrackRecencyPenaltyProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(GenerationStepType.TRACK_RECENCY_PENALTY, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "track_recency";
    }
}
