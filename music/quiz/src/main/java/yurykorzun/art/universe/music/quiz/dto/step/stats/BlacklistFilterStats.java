package yurykorzun.art.universe.music.quiz.dto.step.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class BlacklistFilterStats extends StepRunStats {
    private Map<Long, Long> filteredRecordsByCategory;
}
