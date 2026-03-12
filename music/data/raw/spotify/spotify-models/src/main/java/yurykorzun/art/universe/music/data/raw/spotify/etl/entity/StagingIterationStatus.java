package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StagingIterationStatus implements Coded {

    OPEN(0),
    SEALED(1),
    APPLYING(2),
    COMPLETED(3),
    FAILED(4),
    RETRYING(5);

    private final int code;

    StagingIterationStatus(int code) {
        this.code = code;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), StagingIterationStatus.class);
    }

    @Override
    public Integer getCode() { return code; }

    @Override
    public String getName() { return name(); }
}
