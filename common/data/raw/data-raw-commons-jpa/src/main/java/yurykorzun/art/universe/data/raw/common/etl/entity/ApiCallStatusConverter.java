package yurykorzun.art.universe.data.raw.common.etl.entity;

import yurykorzun.art.universe.common.persistence.converter.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallStatusConverter extends CodedConverter<ApiCallStatus> {

    public ApiCallStatusConverter() {
        super(ApiCallStatus.class);
    }
}
