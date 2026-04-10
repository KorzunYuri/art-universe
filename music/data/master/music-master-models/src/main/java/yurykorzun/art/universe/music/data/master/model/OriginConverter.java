package yurykorzun.art.universe.music.data.master.model;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class OriginConverter extends CodedConverter<Origin> {

    public OriginConverter() {
        super(Origin.class);
    }
}
