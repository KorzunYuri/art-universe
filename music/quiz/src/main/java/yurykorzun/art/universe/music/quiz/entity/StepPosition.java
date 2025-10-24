package yurykorzun.art.universe.music.quiz.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StepPosition implements Coded {
            START(1)
        ,   MIDDLE(2)
        ,   FINAL(3)
    ;

    private final Integer code;

    StepPosition(int code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), StepPosition.class);
    }
}
