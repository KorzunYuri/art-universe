package yurykorzun.art.universe.music.data.raw.lastfm.task.entity;

import yurykorzun.art.universe.common.data.raw.task.entity.TaskType;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskTypeRegistry;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import java.time.Duration;
import java.util.Arrays;

public enum LastfmTaskType implements TaskType, LastfmSpecific {

    TAGS_TOP_TAGS("tags.topTags", Duration.ofDays(1));

    static {
        Arrays.stream(values()).forEach(TaskTypeRegistry::register);
    }

    private final String code;
    private final Duration dueDuration;

    LastfmTaskType(String code, Duration dueDuration) {
        this.code = code;
        this.dueDuration = dueDuration;
    }

    @Override
    public String getCode() {
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
