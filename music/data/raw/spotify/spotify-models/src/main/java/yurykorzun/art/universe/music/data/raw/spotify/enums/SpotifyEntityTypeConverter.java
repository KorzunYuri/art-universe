package yurykorzun.art.universe.music.data.raw.spotify.enums;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class SpotifyEntityTypeConverter extends CodedConverter<SpotifyEntityType> {

    public SpotifyEntityTypeConverter() {
        super(SpotifyEntityType.class);
    }
}
