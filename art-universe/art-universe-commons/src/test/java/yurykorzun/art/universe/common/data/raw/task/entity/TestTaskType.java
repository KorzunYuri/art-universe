package yurykorzun.art.universe.common.data.raw.task.entity;


import java.time.Duration;

public enum TestTaskType implements TaskType {

    TEST_TASK;

    @Override
    public java.time.Duration getDueDuration() {
        return Duration.ofDays(1);
    }

    @Override
    public Integer getCode() {
        return 1;
    }
}