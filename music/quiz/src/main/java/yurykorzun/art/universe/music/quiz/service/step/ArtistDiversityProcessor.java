package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;

@Component
public class ArtistDiversityProcessor extends BaseGenerationStepProcessor {

    public ArtistDiversityProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(GenerationStepType.ARTIST_DIVERSITY, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "artist_diversity";
    }
}
