package yurykorzun.art.universe.music.quiz.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

// TODO use ExecutionStatus instead
public enum GenerationStatus implements Coded {

    PENDING(1),
    COMPLETED(2),
    FAILED(3);

    private final int code;

    GenerationStatus(int code) {
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
        CodedRegistry.register(Arrays.asList(values()), GenerationStatus.class);
    }
}
