package yurykorzun.art.universe.music.quiz.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateGenerationRequest {
    private Integer targetCount;
    private List<GenerationStep> steps;
}
