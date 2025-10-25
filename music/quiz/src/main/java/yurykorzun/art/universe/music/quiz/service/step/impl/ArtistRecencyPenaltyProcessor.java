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
public class ArtistRecencyPenaltyProcessor extends BaseGenerationStepProcessor {

    @PersistenceContext
    private EntityManager entityManager;

    public ArtistRecencyPenaltyProcessor(StepRunRepository stepRunRepository, StepRepository stepRepository) {
        super(StepType.ARTIST_RECENCY_PENALTY, stepRunRepository, stepRepository);
    }

    @Override
    protected String getStepSuffix() {
        return "artistrecency";
    }

    @Override
    protected String processStep(Step step, String inputTableName, String outputTableName, StepRun stepRun) {
        entityManager.createNativeQuery(
            "SELECT p_quiz_gen_tracks_step_artist_recency_penalty(:inputTable, :outputTable)")
            .setParameter("inputTable", inputTableName)
            .setParameter("outputTable", outputTableName)
            .executeUpdate();
        
        return "{}"; // Empty stats, will be calculated separately
    }
}
