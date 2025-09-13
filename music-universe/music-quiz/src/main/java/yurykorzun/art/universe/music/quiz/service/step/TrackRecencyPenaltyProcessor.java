package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class TrackRecencyPenaltyProcessor extends BaseGenerationStepProcessor {
    
    private final PipelineRepository pipelineRepository;
    
    public TrackRecencyPenaltyProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.TRACK_RECENCY_PENALTY);
        this.pipelineRepository = pipelineRepository;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step) {
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.recencyPenalty(parts[0], parts[1], gameId, generationId, stepId);
    }
}
