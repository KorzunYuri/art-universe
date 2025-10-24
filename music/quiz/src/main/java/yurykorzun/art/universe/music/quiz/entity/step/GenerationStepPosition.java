package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum GenerationStepPosition implements Coded {
            START(1)
        ,   MIDDLE(2)
        ,   FINAL(3)
    ;

    private final Integer code;

    GenerationStepPosition(int code) {
        this.code = code;
    }

    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), GenerationStepPosition.class);
    }
}
