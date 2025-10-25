package yurykorzun.art.universe.music.quiz.dto.step.stats;

import lombok.Data;

@Data
public abstract class StepRunStats {
    private Long inputRecords;
    private Long filteredRecords;
    private Long outputRecords;
    private Long inputArtists;
    private Long filteredArtists;
    private Long outputArtists;
    private Long executionTimeMs;
}
