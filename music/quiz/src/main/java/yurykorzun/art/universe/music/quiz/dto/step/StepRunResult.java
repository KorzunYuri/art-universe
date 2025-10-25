package yurykorzun.art.universe.music.quiz.dto.step;

import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.quiz.dto.step.stats.StepRunStats;

@Data
@Builder
public class StepRunResult {
    private String outputTableName;
    private StepRunStats resultStats;
}
