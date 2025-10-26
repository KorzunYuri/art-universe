package yurykorzun.art.universe.music.quiz.service.step.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.dto.step.StepRunResult;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class ApprovedFilterProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    public ApprovedFilterProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository, ObjectMapper objectMapper) {
        super(StepType.APPROVED_FILTER, stepRunRepository, stepRepository, objectMapper);
    }

    @Override
    protected String getStepSuffix() {
        return "approved";
    }

    @Override
    protected StepRunResult processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_approved_filter(:inputTable, :outputTable)")
            .setParameter("inputTable", inputTableName)
            .setParameter("outputTable", outputTableName)
            .executeUpdate();
        
        return StepRunResult.builder()
            .outputTableName(outputTableName)
            .build();
    }
}
