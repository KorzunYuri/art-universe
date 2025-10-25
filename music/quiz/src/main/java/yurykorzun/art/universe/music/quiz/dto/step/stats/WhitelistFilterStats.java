package yurykorzun.art.universe.music.quiz.dto.step.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class WhitelistFilterStats extends StepRunStats {
    private Map<Long, Long> outputRecordsByCategory;
    private Map<Long, Long> outputArtistsByCategory;
}
