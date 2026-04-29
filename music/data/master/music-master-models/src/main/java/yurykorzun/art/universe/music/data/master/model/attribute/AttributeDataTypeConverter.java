package yurykorzun.art.universe.music.data.master.model.attribute;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class AttributeDataTypeConverter extends CodedConverter<AttributeDataType> {

    public AttributeDataTypeConverter() {
        super(AttributeDataType.class);
    }
}
