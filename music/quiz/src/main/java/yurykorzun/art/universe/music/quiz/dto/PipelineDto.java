package yurykorzun.art.universe.music.quiz.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PipelineDto {
    private Long id;
    private Boolean immutable;
    private List<PipelineStepDto> steps;
}
