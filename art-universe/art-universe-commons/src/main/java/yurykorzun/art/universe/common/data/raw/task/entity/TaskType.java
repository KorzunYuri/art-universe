package yurykorzun.art.universe.common.data.raw.task.entity;

import yurykorzun.art.universe.common.Coded;

import java.time.Duration;


public interface TaskType extends Coded {
    Duration getDueDuration();
}
