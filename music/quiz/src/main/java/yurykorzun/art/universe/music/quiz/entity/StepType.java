package yurykorzun.art.universe.music.quiz.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum StepType implements Coded {

    APPROVED_FILTER(1, 1),
    BLACKLIST_FILTER(2, 1),
    WHITELIST_FILTER(3, 1),
    TRACK_RECENCY_PENALTY(4, 1),
    ARTIST_RECENCY_PENALTY(5, 1),
    ARTIST_DIVERSITY(6, 1),
    FINAL_SELECTION(7, 1),
    FINAL_CATEGORIES_BALANCER(8, 1),
    START_DATASOURCE(9, 1);

    private final Integer code;
    private final Integer version;

    StepType(int code, int version) {
        this.code = code;
        this.version = version;
    }
    
    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), StepType.class);
    }
}
