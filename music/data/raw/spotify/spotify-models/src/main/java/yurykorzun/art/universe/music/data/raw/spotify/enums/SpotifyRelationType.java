package yurykorzun.art.universe.music.data.raw.spotify.enums;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum SpotifyRelationType implements Coded {

    ARTIST_ALBUM(1),
    ALBUM_ARTIST(2),
    ALBUM_TRACK(3),
    TRACK_ARTIST(4),
    ARTIST_GENRE(5);

    private final int code;

    SpotifyRelationType(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), SpotifyRelationType.class);
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
