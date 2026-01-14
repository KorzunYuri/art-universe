package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class GenerationStatusConverter extends CodedConverter<GenerationStatus> {

    public GenerationStatusConverter() {
        super(GenerationStatus.class);
    }
}
