package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.finish.FinalSelectionStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class FinalSelectionProcessor extends BaseGenerationStepProcessor<FinalSelectionStep> {
    
    private final PipelineRepository pipelineRepository;
    
    public FinalSelectionProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.FINAL_SELECTION);
        this.pipelineRepository = pipelineRepository;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, FinalSelectionStep step) {
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.finalSelection(parts[0], parts[1], gameId, generationId, stepOrder, step.getTargetCount());
    }
}
