package yurykorzun.art.universe.music.data.approved.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum DataSource implements Coded {

        LASTFM(1)
    ,   SPOTIFY(2)
    ,   MUSICBRAINZ(3)
    ;

    private final int code;

    DataSource(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), DataSource.class);
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return this.name();
    }
}
