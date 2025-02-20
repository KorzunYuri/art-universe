package yurykorzun.art.universe.common.persistence.converters;

import yurykorzun.art.universe.common.persistence.entity.RequestStatus;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class RequestStatusConverter implements AttributeConverter<RequestStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(RequestStatus status) {
        return status.getId();
    }

    @Override
    public RequestStatus convertToEntityAttribute(Integer id) {
        return RequestStatus.getById(id);
    }
}
