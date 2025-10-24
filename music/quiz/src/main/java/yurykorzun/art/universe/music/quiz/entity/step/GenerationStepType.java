package yurykorzun.art.universe.music.quiz.entity.step;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

@Getter
public enum GenerationStepType implements Coded {

    APPROVED_FILTER(1),
    BLACKLIST_FILTER(2),
    WHITELIST_FILTER(3),
    TRACK_RECENCY_PENALTY(4),
    ARTIST_RECENCY_PENALTY(5),
    ARTIST_DIVERSITY(6),
    FINAL_SELECTION(7),
    FINAL_CATEGORIES_BALANCER(8),
    START_DATASOURCE(9);;

    private final Integer code;

    GenerationStepType(int code) {
        this.code = code;
    }
    
    @Override
    public String getName() {
        return name();
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), GenerationStepType.class);
    }
}
