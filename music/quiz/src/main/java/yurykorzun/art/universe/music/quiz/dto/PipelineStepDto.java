package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.quiz.entity.StepType;

@Data
@Builder
public class PipelineStepDto {
    private Long id;
    private StepType type;
    private Integer algVersion;
    private String cfgData;
    private String previewData;
    private Integer ord;
    private String resultTableName; // from lastStepRun
    private String resultStats; // from lastStepRun
}
