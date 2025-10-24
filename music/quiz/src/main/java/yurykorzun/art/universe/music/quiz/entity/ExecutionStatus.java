package yurykorzun.art.universe.music.quiz.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

public enum ExecutionStatus implements Coded {

    PENDING(1),
    STARTED(2),
    COMPLETED(3),
    FAILED(4);

    private final int code;

    ExecutionStatus(int code) {
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
        CodedRegistry.register(Arrays.asList(values()), ExecutionStatus.class);
    }
}
