package yurykorzun.art.universe.music.data.raw.spotify.enums;

import lombok.Getter;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.domain.entity.EntityType;

import java.util.Arrays;

@Getter
public enum SpotifyEntityType implements EntityType {

    ARTIST(1),
    ALBUM(2),
    TRACK(3),
    GENRE(4);

    private final int code;

    SpotifyEntityType(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), SpotifyEntityType.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }
}
