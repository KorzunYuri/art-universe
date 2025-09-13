package yurykorzun.art.universe.music.quiz.dto;

import lombok.Data;
import yurykorzun.art.universe.music.quiz.entity.GenerationStepType;

import java.util.Map;

@Data
public class GenerationStep {
    private GenerationStepType type;
    private Map<String, Object> params;
}
