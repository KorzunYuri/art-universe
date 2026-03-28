package yurykorzun.art.universe.music.data.master.entity.attribute;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class AttributeTargetTypeConverter extends CodedConverter<AttributeTargetType> {

    public AttributeTargetTypeConverter() {
        super(AttributeTargetType.class);
    }
}
