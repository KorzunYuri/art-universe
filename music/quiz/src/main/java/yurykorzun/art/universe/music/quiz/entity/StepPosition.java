package yurykorzun.art.universe.music.quiz.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StepPosition implements Coded {
    INITIAL(1),     // Doesn't require input, produces initial dataset
    TRANSFORM(2);   // Requires input, transforms data

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
