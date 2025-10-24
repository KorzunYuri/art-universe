package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.middle.ApprovedFilterStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class ApprovedFilterProcessor extends BaseGenerationStepProcessor<ApprovedFilterStep> {
    
    private final PipelineRepository pipelineRepository;
    
    public ApprovedFilterProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.APPROVED_FILTER);
        this.pipelineRepository = pipelineRepository;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, ApprovedFilterStep step) {
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.approvedFilter(parts[0], parts[1], gameId, generationId, stepOrder);
    }
}
