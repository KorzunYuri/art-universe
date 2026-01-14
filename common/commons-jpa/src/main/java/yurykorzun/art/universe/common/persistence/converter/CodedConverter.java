package yurykorzun.art.universe.common.persistence.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

@Converter
public class CodedConverter <T extends Coded> implements AttributeConverter<T, Integer> {

    private final Class<T> type;

    public CodedConverter(Class<T> type) {
        this.type = type;
    }

    @Override
    public Integer convertToDatabaseColumn(T coded) {
        if (coded == null) {
            return null;
        }
        return coded.getCode();
    }

    @Override
    public T convertToEntityAttribute(Integer code) {
        if (code == null) return null;
        return CodedRegistry.getByCode(code, this.type)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Unknown %s code: %s", type, code)));
    }
}
