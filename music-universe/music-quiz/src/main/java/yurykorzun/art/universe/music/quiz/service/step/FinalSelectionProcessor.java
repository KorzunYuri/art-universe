package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.GenerationStep;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class FinalSelectionProcessor extends BaseGenerationStepProcessor {
    
    private final PipelineRepository pipelineRepository;
    
    public FinalSelectionProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.FINAL_SELECTION);
        this.pipelineRepository = pipelineRepository;
    }

    @Override
    protected void validateParams(GenerationStep step) {
        if (step.getParams() == null || !step.getParams().containsKey("targetCount")) {
            throw new IllegalArgumentException("Final selection step requires 'targetCount' parameter");
        }
        
        Object targetCountObj = step.getParams().get("targetCount");
        if (!(targetCountObj instanceof Number)) {
            throw new IllegalArgumentException("Parameter 'targetCount' must be a number");
        }
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepId, GenerationStep step) {
        Integer targetCount = ((Number) step.getParams().get("targetCount")).intValue();
        
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.finalSelection(parts[0], parts[1], gameId, generationId, stepId, targetCount);
    }
}
