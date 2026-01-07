package yurykorzun.art.universe.common.data.raw.api.client.entity;

import yurykorzun.art.universe.common.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallStatusConverter extends CodedConverter<ApiCallStatus> {

    public ApiCallStatusConverter() {
        super(ApiCallStatus.class);
    }
}
