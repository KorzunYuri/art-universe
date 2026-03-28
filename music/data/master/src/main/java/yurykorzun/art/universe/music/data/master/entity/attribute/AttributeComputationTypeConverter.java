package yurykorzun.art.universe.music.data.master.entity.attribute;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class AttributeComputationTypeConverter extends CodedConverter<AttributeComputationType> {

    public AttributeComputationTypeConverter() {
        super(AttributeComputationType.class);
    }
}
