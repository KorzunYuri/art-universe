package yurykorzun.art.universe.art.data.models.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class ArtEntityTypeConverter extends CodedConverter<ArtEntityType> {

    public ArtEntityTypeConverter() {
        super(ArtEntityType.class);
    }
}
