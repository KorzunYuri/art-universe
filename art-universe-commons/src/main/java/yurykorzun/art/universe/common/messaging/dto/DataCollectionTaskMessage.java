package yurykorzun.art.universe.common.messaging.dto;

import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskType;

import java.time.Instant;

public record DataCollectionTaskMessage (
        long id,
        DataCollectionTaskType type,
        Instant dueDttm
) {
}
