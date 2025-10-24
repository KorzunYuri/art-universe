package yurykorzun.art.universe.music.quiz.entity.step;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class GenerationStepTypeConverter extends CodedConverter<GenerationStepType> {

    public GenerationStepTypeConverter() {
        super(GenerationStepType.class);
    }
}
