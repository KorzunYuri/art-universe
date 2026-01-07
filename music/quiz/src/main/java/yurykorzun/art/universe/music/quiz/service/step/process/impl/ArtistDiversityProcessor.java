package yurykorzun.art.universe.music.quiz.service.step.process.impl;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.service.step.process.StepProcessorRegistry;

@Component
public class ArtistDiversityProcessor extends BasicStepProcessor {
    public ArtistDiversityProcessor(StepProcessorRegistry registry) {
        super(registry);
    }

    @Override
    public StepType getStepType() {
        return StepType.ARTIST_DIVERSITY;
    }

    @Override
    protected StepRunResult executeStepLogic(Step step, String inputTableName, String stepTableNameBase, StepRun stepRun) {
        String outputTableName = stepTableNameBase + "_artist_diversity";
        entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_diversity(:inputTable, :outputTable)")
            .setParameter("inputTable", inputTableName)
            .setParameter("outputTable", outputTableName)
            .getSingleResult();
        
        return StepRunResult.builder()
            .outputTableName(outputTableName)
            .build();
    }
}
