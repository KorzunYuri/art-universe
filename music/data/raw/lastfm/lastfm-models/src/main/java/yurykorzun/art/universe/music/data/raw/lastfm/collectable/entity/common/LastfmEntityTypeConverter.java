package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class LastfmEntityTypeConverter extends CodedConverter<LastfmEntityType> {

    public LastfmEntityTypeConverter() {
        super(LastfmEntityType.class);
    }
}
