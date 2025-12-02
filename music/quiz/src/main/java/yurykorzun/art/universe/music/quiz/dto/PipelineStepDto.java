package yurykorzun.art.universe.music.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.quiz.entity.StepType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineStepDto {
    private Long id;
    private StepType type;
    private Integer algVersion;
    private String cfgData;
    private String previewData;
    private Integer ord;
    private String resultStats; // from lastStepRun
}
