package yurykorzun.art.universe.music.quiz.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StepType implements Coded {

    APPROVED_FILTER(1, 1, StepPosition.TRANSFORM),
    BLACKLIST_FILTER(2, 1, StepPosition.TRANSFORM),
    WHITELIST_FILTER(3, 1, StepPosition.TRANSFORM),
    TRACK_RECENCY_PENALTY(4, 1, StepPosition.TRANSFORM),
    ARTIST_RECENCY_PENALTY(5, 1, StepPosition.TRANSFORM),
    ARTIST_DIVERSITY(6, 1, StepPosition.TRANSFORM),
    FINAL_LIMITER(7, 1, StepPosition.TRANSFORM),
    FINAL_CATEGORIES_BALANCER(8, 1, StepPosition.TRANSFORM),
    START_DATASOURCE(9, 1, StepPosition.INITIAL);

    private final Integer code;
    private final Integer version;
    private final StepPosition position;

    StepType(int code, int version, StepPosition position) {
        this.code = code;
        this.version = version;
        this.position = position;
    }

    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), StepType.class);
    }
}
