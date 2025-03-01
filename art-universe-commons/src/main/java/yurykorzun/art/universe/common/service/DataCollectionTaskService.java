package yurykorzun.art.universe.common.service;

import yurykorzun.art.universe.common.dto.DataCollectionTaskCreateRequest;
import yurykorzun.art.universe.common.dto.DataCollectionTaskCreateResponse;
import yurykorzun.art.universe.common.persistence.entity.TaskStatus;

public interface DataCollectionTaskService {
    DataCollectionTaskCreateResponse createRequest(DataCollectionTaskCreateRequest request);
    void setStatus(long id, TaskStatus status);
}
