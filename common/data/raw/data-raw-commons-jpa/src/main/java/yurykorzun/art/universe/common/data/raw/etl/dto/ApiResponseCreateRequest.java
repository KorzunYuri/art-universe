package yurykorzun.art.universe.common.data.raw.etl.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ApiResponseCreateRequest {
    private String responseBody;
}
