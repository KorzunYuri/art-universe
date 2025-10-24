package yurykorzun.art.universe.music.quiz.entity.step.start;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StartDatasourceStep extends StartGenerationStep {

    private final String datasource;
    
    public StartDatasourceStep() {
        super(GenerationStepType.START_DATASOURCE);
        this.datasource = "mu_view.v_track"; // захардкожено
    }
}
