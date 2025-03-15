package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import yurykorzun.art.universe.common.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class LastfmEntityTypeConverter extends CodedConverter<LastfmEntityType> {

    public LastfmEntityTypeConverter() {
        super(LastfmEntityType.class);
    }
}
