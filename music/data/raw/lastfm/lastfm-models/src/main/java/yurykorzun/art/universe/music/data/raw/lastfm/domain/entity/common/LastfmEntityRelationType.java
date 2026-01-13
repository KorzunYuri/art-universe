package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum LastfmEntityRelationType implements Coded {

    SIMILARITY(1)
    ;

    private final int code;

    LastfmEntityRelationType(int code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), LastfmEntityRelationType.class);
    }
}
