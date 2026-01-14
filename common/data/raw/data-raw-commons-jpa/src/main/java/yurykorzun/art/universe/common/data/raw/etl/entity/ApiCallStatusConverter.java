package yurykorzun.art.universe.common.data.raw.etl.entity;

import yurykorzun.art.universe.common.domain.converter.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallStatusConverter extends CodedConverter<ApiCallStatus> {

    public ApiCallStatusConverter() {
        super(ApiCallStatus.class);
    }
}
