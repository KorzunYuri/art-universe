package yurykorzun.art.universe.music.data.raw.spotify.enums;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum SpotifyAlbumType implements Coded {

    ALBUM(1),
    SINGLE(2),
    COMPILATION(3);

    private final int code;

    SpotifyAlbumType(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), SpotifyAlbumType.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    public static SpotifyAlbumType fromSpotifyString(String albumType) {
        if (albumType == null) return null;
        return switch (albumType.toLowerCase()) {
            case "album" -> ALBUM;
            case "single" -> SINGLE;
            case "compilation" -> COMPILATION;
            default -> null;
        };
    }
}
