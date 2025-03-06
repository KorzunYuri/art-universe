package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import yurykorzun.art.universe.common.data.raw.task.entity.TaskTypeRegistry;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallTypeConverter implements AttributeConverter<ApiCallType, String> {

    @Override
    public String convertToDatabaseColumn(ApiCallType apiCallType) {
        return apiCallType.getMethod();
    }

    @Override
    public ApiCallType convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return ApiCallTypeRegistry.getByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Unknown ApiCallType code: " + code));
    }
}
