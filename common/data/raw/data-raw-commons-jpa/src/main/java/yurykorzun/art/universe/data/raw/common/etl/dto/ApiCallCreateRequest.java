package yurykorzun.art.universe.data.raw.common.etl.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;

import java.time.Instant;

@SuperBuilder
@Getter
public abstract class ApiCallCreateRequest {
    public abstract ApiCallType getType();
    private Instant dueDttm;
}
