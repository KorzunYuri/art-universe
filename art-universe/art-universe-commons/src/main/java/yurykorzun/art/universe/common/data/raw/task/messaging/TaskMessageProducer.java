package yurykorzun.art.universe.common.data.raw.task.messaging;

import yurykorzun.art.universe.common.data.raw.task.dto.TaskRunRequest;

public interface TaskMessageProducer {

    void send(TaskRunRequest message);

}
