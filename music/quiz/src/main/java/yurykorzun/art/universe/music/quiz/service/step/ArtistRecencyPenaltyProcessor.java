package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.middle.ArtistRecencyPenaltyStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class ArtistRecencyPenaltyProcessor extends BaseGenerationStepProcessor<ArtistRecencyPenaltyStep> {
    
    private final PipelineRepository pipelineRepository;
    
    public ArtistRecencyPenaltyProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.ARTIST_RECENCY_PENALTY);
        this.pipelineRepository = pipelineRepository;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, ArtistRecencyPenaltyStep step) {
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.artistRecencyPenalty(parts[0], parts[1], gameId, generationId, stepOrder);
    }
}
