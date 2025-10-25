package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.dto.step.config.FinalSelectionStepConfig;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class FinalSelectionProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final ObjectMapper objectMapper;

    public FinalSelectionProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.FINAL_SELECTION, stepRunRepository, stepRepository);
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getStepSuffix() {
        return "final";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            FinalSelectionStepConfig config = objectMapper.readValue(step.getCfgData(), FinalSelectionStepConfig.class);
            
            entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_final_selection(:inputTable, :outputTable, :targetCount)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("targetCount", config.getTargetCount())
                .executeUpdate();
                
            return "{}"; // Empty stats, will be calculated separately
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }
}
