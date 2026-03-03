package yurykorzun.art.universe.music.data.raw.spotify.enums;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

@Converter(autoApply = true)
public class SpotifyRelationTypeConverter extends CodedConverter<SpotifyRelationType> {

    public SpotifyRelationTypeConverter() {
        super(SpotifyRelationType.class);
    }
}
