package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.Data;
import yurykorzun.art.universe.music.quiz.dto.GenerationStepDto;

@Data
public abstract class GenerationStep {
    private final GenerationStepType type;
    
    protected GenerationStep(GenerationStepType type) {
        this.type = type;
    }

    public abstract GenerationStepPosition getPosition();
    
    public static GenerationStep fromDto(GenerationStepDto dto) {
        return GenerationStepMapper.fromDto(dto);
    }
}
