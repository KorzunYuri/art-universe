package yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class CoordinationStatusConverter extends CodedConverter<CoordinationStatus> {

    public CoordinationStatusConverter() {
        super(CoordinationStatus.class);
    }
}
