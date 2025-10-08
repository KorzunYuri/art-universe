package yurykorzun.art.universe.common.data.raw.api.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;

import java.time.Instant;

@SuperBuilder
@Getter
public abstract class ApiCallCreateRequest {
    public abstract ApiCallType getType();
    private Instant dueDttm;
}
