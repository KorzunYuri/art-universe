package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.dto.step.config.FinalSelectionStepConfig;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class FinalSelectionProcessor extends BaseGenerationStepProcessor {

    public FinalSelectionProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.FINAL_SELECTION, stepRunRepository, stepRepository, objectMapper);
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }
    
    private FinalSelectionStepConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, FinalSelectionStepConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for final selection step", e);
        }
    }

    @Override
    protected String getStepSuffix() {
        return "final";
    }

    @Override
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        try {
            FinalSelectionStepConfig config = parseConfig(step.getCfgData());
            
            entityManager.createNativeQuery(
                "SELECT p_quiz_gen_tracks_step_final_selection(:inputTable, :outputTable, :targetCount)")
                .setParameter("inputTable", inputTableName)
                .setParameter("outputTable", outputTableName)
                .setParameter("targetCount", config.targetCount())
                .executeUpdate();
                
            return StepRunResult.builder()
                .outputTableName(outputTableName)
                .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse step configuration", e);
        }
    }
}
