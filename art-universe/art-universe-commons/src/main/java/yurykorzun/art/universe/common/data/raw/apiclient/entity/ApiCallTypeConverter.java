package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import yurykorzun.art.universe.common.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallTypeConverter extends CodedConverter<ApiCallType> {

    public ApiCallTypeConverter() {
        super(ApiCallType.class);
    }
}
