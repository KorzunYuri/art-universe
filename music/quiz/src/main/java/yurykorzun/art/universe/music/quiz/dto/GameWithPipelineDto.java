package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class GameWithPipelineDto {
    private Long id;
    private Instant createdAt;
    private PipelineDto pipeline;
}
