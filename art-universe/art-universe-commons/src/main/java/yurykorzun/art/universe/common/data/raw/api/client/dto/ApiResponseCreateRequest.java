package yurykorzun.art.universe.common.data.raw.api.client.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallType;

@SuperBuilder
@Getter
public class ApiResponseCreateRequest {
    private long apiCallId;
    private ApiCallType apiCallType;
    private String responseBody;
}
