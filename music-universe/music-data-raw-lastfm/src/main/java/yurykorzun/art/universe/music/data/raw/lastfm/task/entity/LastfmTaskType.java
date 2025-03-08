package yurykorzun.art.universe.music.data.raw.lastfm.task.entity;

import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import java.time.Duration;
import java.util.Arrays;

public enum LastfmTaskType implements TaskType, LastfmSpecific {

    TAGS_TOP_TAGS(1, "tags.topTags", Duration.ofDays(1));

    private final int code;
    private final String name;
    private final Duration dueDuration;

    LastfmTaskType(int code, String name, Duration dueDuration) {
        this.code = code;
        this.name = name;
        this.dueDuration = dueDuration;
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), TaskType.class);
    }

    @Override
    public Integer getCode() {
        return this.code;
    }

    @Override
    public Duration getDueDuration() {
        return this.dueDuration;
    }

    @Override
    public String getTypeName() {
        return "task";
    }
}
