package yurykorzun.art.universe.music.quiz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PipelineDto {
    private Long id;
    private Boolean immutable;
    private List<PipelineStepDto> steps;
}
