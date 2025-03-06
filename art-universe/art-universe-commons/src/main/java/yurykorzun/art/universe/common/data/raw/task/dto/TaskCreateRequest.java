package yurykorzun.art.universe.common.data.raw.task.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.task.entity.TaskType;

@SuperBuilder
@Getter
public class TaskCreateRequest<T extends TaskType> {

    @NonNull
    private T taskType;

}
