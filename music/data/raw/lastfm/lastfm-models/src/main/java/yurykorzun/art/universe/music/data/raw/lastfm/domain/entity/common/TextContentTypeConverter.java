package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class TextContentTypeConverter extends CodedConverter<TextContentType> {

    public TextContentTypeConverter() {
        super(TextContentType.class);
    }
}
