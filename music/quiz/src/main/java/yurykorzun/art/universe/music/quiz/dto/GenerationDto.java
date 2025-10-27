package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;
import yurykorzun.art.universe.music.quiz.entity.GenerationStatus;

import java.time.Instant;

@Data
@Builder
public class GenerationDto {
    private Long id;
    private Long gameId;
    private Long pipelineId;
    private Long pipelineRunId;
    private Integer targetCount;
    private GenerationStatus status;
    private Boolean approved;
    private String resultTableName;
    private Instant createdAt;
}
