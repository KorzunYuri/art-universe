package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class LastfmEntityRelationTypeConverter extends CodedConverter<LastfmEntityRelationType> {

    public LastfmEntityRelationTypeConverter() {
        super(LastfmEntityRelationType.class);
    }
}
