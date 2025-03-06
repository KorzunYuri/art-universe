package yurykorzun.art.universe.common.data.raw.task.entity;


import java.time.Duration;

public enum TestTaskType implements TaskType {

    TEST_TASK;

    @Override
    public java.time.Duration getDueDuration() {
        return Duration.ofDays(1);
    }

    @Override
    public String getCode() {
        return "test-code";
    }

    @Override
    public String getDataSourceCode() {
        return "test-ds";
    }

    @Override
    public String getDomainCode() {
        return "music";
    }

    @Override
    public String getTypeName() {
        return "test-task";
    }

}