package yurykorzun.art.universe.music.quiz.dto.step.config;

import yurykorzun.art.universe.music.quiz.entity.StepType;

public record StartDatasourceStepConfig() implements StepConfig {

    public final static String DEFAULT_DATASOURCE = "mu_quiz.ds_mu_v_track";

    public String getDatasource() {
        return DEFAULT_DATASOURCE;
    }

    @Override
    public StepType getType() {
        return StepType.START_DATASOURCE;
    }
}
