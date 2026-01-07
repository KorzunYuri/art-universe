package yurykorzun.art.universe.music.data.raw.lastfm.coordination.entity;

import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;

import java.util.Arrays;

/**
 * Status of the coordination state for task execution.
 * Stored as INTEGER in database (1=NORMAL, 2=REQUESTED, 3=RUNNING).
 */
public enum CoordinationStatus implements Coded {
    /**
     * Normal operation - tasks are allowed to execute.
     */
    NORMAL(1),

    /**
     * Maintenance has been requested - waiting for running tasks to complete.
     * New tasks are blocked during this state.
     */
    REQUESTED(2),

    /**
     * Maintenance is in progress - all tasks are blocked.
     */
    RUNNING(3);

    private final int code;

    CoordinationStatus(int code) {
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
        CodedRegistry.register(Arrays.asList(values()), CoordinationStatus.class);
    }
}
