package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

@Component
public class ApprovedFilterProcessor extends BasicStepProcessor {
    public ApprovedFilterProcessor(StepProcessorRegistry registry) {
        super(registry);
    }

    @Override
    public StepType getStepType() {
        return StepType.APPROVED_FILTER;
    }

    @Override
    protected StepRunResult executeStepLogic(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
        String outputTableName = stepTableNameBase + "_approved";
        entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_approved_filter(:inputTable, :outputTable)")
            .setParameter("inputTable", inputTableName)
            .setParameter("outputTable", outputTableName)
            .getSingleResult();
        
        return StepRunResult.builder()
            .outputTableName(outputTableName)
            .build();
    }
}
