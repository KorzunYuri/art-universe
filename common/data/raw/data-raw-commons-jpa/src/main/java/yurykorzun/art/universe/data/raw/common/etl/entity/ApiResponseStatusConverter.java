package yurykorzun.art.universe.data.raw.common.etl.entity;

import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

public class ApiResponseStatusConverter extends CodedConverter<ApiResponseStatus> {

    public ApiResponseStatusConverter() {
        super(ApiResponseStatus.class);
    }
}
