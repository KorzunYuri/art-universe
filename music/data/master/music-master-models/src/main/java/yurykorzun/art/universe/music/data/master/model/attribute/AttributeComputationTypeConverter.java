package yurykorzun.art.universe.music.data.master.model.attribute;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class AttributeComputationTypeConverter extends CodedConverter<AttributeComputationType> {

    public AttributeComputationTypeConverter() {
        super(AttributeComputationType.class);
    }
}
