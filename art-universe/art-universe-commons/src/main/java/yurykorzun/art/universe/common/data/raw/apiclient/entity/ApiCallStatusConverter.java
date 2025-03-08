package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import yurykorzun.art.universe.common.CodedConverter;

import javax.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallStatusConverter extends CodedConverter<ApiCallStatus> {

    public ApiCallStatusConverter() {
        super(ApiCallStatus.class);
    }
}
