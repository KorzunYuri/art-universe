package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class LastfmEntityRelationTypeConverter extends CodedConverter<LastfmEntityRelationType> {

    public LastfmEntityRelationTypeConverter() {
        super(LastfmEntityRelationType.class);
    }
}
