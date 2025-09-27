package yurykorzun.art.universe.music.quiz.service.step;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;
import yurykorzun.art.universe.music.quiz.entity.step.ArtistDiversityStep;
import yurykorzun.art.universe.music.quiz.repository.PipelineRepository;

@Component
public class ArtistDiversityProcessor extends BaseGenerationStepProcessor<ArtistDiversityStep> {
    
    private final PipelineRepository pipelineRepository;
    
    public ArtistDiversityProcessor(PipelineRepository pipelineRepository) {
        super(GenerationStepType.ARTIST_DIVERSITY);
        this.pipelineRepository = pipelineRepository;
    }
    
    @Override
    protected String processStep(String inputTable, Long gameId, Long generationId, Integer stepOrder, ArtistDiversityStep step) {
        String[] parts = inputTable.split("\\.");
        return pipelineRepository.artistDiversity(parts[0], parts[1], gameId, generationId, stepOrder);
    }
}
