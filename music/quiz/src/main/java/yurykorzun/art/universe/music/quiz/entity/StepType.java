package yurykorzun.art.universe.music.quiz.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StepType implements Coded {

    APPROVED_FILTER(1, 1, StepPosition.MIDDLE),
    BLACKLIST_FILTER(2, 1, StepPosition.MIDDLE),
    WHITELIST_FILTER(3, 1, StepPosition.MIDDLE),
    TRACK_RECENCY_PENALTY(4, 1, StepPosition.MIDDLE),
    ARTIST_RECENCY_PENALTY(5, 1, StepPosition.MIDDLE),
    ARTIST_DIVERSITY(6, 1, StepPosition.MIDDLE),
    FINAL_SELECTION(7, 1, StepPosition.FINAL),
    FINAL_CATEGORIES_BALANCER(8, 1, StepPosition.FINAL),
    START_DATASOURCE(9, 1, StepPosition.START);

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
