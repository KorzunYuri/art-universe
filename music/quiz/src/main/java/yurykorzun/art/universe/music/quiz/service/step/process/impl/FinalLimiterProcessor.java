package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.dto.step.config.FinalLimiterStepConfig;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

@Component
public class FinalLimiterProcessor extends BasicStepProcessor {
    private final ObjectMapper objectMapper;

    public FinalLimiterProcessor(StepProcessorRegistry registry, ObjectMapper objectMapper) {
        super(registry);
        this.objectMapper = objectMapper;
    }

    @Override
    public StepType getStepType() {
        return StepType.FINAL_LIMITER;
    }

    @Override
    public void validateConfiguration(String cfgData) {
        parseConfig(cfgData);
    }
    
    private FinalLimiterStepConfig parseConfig(String cfgData) {
        try {
            return objectMapper.readValue(cfgData, FinalLimiterStepConfig.class);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration for final limiter step", e);
        }
    }

    @Override
    public StepRunResult processStep(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
        String outputTableName = stepTableNameBase + "_limiter";
        try {
            FinalLimiterStepConfig config = parseConfig(step.getCfgData());
            
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
