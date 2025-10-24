package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class StepTypeConverter extends CodedConverter<StepType> {

    public StepTypeConverter() {
        super(StepType.class);
    }
}
