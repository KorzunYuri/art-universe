package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum SearchAttemptStatus implements Coded {

    PENDING(1),
    SEARCHED(2),
    MATCHED(3),
    NO_MATCH(4),
    BOUND(5);

    private final int code;

    SearchAttemptStatus(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), SearchAttemptStatus.class);
    }

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getName() { return name(); }
}
