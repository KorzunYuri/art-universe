package yurykorzun.art.universe.music.quiz.dto;

import lombok.Data;
import yurykorzun.art.universe.music.quiz.entity.StepType;

import java.util.Map;

@Data
public class GenerationStepDto {
    private StepType type;
    private Map<String, Object> params;
}
