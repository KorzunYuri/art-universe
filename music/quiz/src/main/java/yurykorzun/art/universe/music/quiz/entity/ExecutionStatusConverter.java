package yurykorzun.art.universe.music.quiz.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.domain.converter.CodedConverter;

@Converter(autoApply = true)
public class ExecutionStatusConverter extends CodedConverter<ExecutionStatus> {

    public ExecutionStatusConverter() {
        super(ExecutionStatus.class);
    }
}
