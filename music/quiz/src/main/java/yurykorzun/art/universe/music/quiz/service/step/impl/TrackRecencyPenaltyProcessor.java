package yurykorzun.art.universe.music.quiz.service.step.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.quiz.entity.Step;
import yurykorzun.art.universe.music.quiz.entity.StepRun;
import yurykorzun.art.universe.music.quiz.entity.StepType;
import yurykorzun.art.universe.music.quiz.repository.StepRepository;
import yurykorzun.art.universe.music.quiz.repository.StepRunRepository;
import yurykorzun.art.universe.music.quiz.service.step.BaseGenerationStepProcessor;

@Component
public class TrackRecencyPenaltyProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    public TrackRecencyPenaltyProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(StepType.TRACK_RECENCY_PENALTY, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "trackrecency";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        return (String) entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_track_recency_penalty(:inputTable, :outputTable)")
            .setParameter("inputTable", inputTableName)
            .setParameter("outputTable", outputTableName)
            .getSingleResult();
    }
}
