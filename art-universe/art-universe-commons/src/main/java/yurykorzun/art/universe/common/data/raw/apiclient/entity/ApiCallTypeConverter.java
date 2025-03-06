package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallTypeConverter implements AttributeConverter<ApiCallType, String> {

    @Override
    public String convertToDatabaseColumn(ApiCallType apiCallType) {
        return apiCallType.getMethod();
    }

    @Override
    public ApiCallType convertToEntityAttribute(String s) {
        return null;
    }
}
