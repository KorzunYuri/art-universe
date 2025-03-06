package yurykorzun.art.universe.common.data.raw.task.dto;

import yurykorzun.art.universe.common.data.raw.task.entity.TaskType;

import java.time.Instant;

public record TaskRunRequest(
        long id,
        TaskType type,
        Instant dueDttm
) {
}
