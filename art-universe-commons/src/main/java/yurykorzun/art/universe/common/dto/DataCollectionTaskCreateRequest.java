package yurykorzun.art.universe.common.dto;

import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskType;

import java.time.Instant;

public record DataCollectionTaskCreateRequest(
        DataCollectionTaskType type,
        Instant dueDttm
) {
}
