package yurykorzun.art.universe.common.data.raw.etl.entity;

import yurykorzun.art.universe.common.domain.converter.CodedConverter;

public class ApiResponseStatusConverter extends CodedConverter<ApiResponseStatus> {

    public ApiResponseStatusConverter() {
        super(ApiResponseStatus.class);
    }
}
