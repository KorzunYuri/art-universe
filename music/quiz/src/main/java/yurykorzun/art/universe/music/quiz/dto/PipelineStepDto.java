package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.quiz.entity.step.GenerationStepType;

@Data
@Builder
public class PipelineStepDto {
    private Long id;
    private GenerationStepType type;
    private Integer algVersion;
    private String cfgData;
    private String previewData;
    private String resultTableName;
    private String resultStats;
    private Integer ord;
}
