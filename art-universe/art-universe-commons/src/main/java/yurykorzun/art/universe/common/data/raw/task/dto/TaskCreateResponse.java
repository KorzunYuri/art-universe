package yurykorzun.art.universe.common.data.raw.task.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class TaskCreateResponse {

    @NonNull
    private Long id;

}
