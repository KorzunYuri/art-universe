package yurykorzun.art.universe.data.raw.common.etl.dto;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ApiResponseCreateRequest {
    private String responseBody;
}
