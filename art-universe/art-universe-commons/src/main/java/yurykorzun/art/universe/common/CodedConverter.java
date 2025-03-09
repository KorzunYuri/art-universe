package yurykorzun.art.universe.common;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CodedConverter <T extends Coded> implements AttributeConverter<T, Integer> {

    private final Class<T> type;

    public CodedConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public Integer convertToDatabaseColumn(T coded) {
        return coded.getCode();
    }

    @Override
    public T convertToEntityAttribute(Integer code) {
        if (code == null) return null;
        return CodedRegistry.getByCode(code, this.type)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown %s code: %s", type, code)));
    }
}
