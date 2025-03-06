package yurykorzun.art.universe.common.data.raw.apiclient.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ApiCallStatusConverter implements AttributeConverter<ApiCallStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(ApiCallStatus status) {
        return status.getId();
    }

    @Override
    public ApiCallStatus convertToEntityAttribute(Integer id) {
        return ApiCallStatus.getById(id);
    }
}
