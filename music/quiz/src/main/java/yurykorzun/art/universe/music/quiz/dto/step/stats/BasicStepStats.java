package yurykorzun.art.universe.music.quiz.dto.step.stats;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BasicStepStats extends StepRunStats {
    // No additional fields for basic processors
    
    public static void copyBasicStats(StepRunStats source, StepRunStats target) {
        target.setInputRecords(source.getInputRecords());
        target.setFilteredRecords(source.getFilteredRecords());
        target.setOutputRecords(source.getOutputRecords());
        target.setInputArtists(source.getInputArtists());
        target.setFilteredArtists(source.getFilteredArtists());
        target.setOutputArtists(source.getOutputArtists());
        target.setExecutionTimeMs(source.getExecutionTimeMs());
    }
}
