package yurykorzun.art.universe.common.persistence.converters;

import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskType;
import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskTypeRegistry;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class DataCollectionTaskTypeConverter implements AttributeConverter<DataCollectionTaskType, String> {

    @Override
    public String convertToDatabaseColumn(DataCollectionTaskType taskType) {
        return taskType != null ? taskType.getCode() : null;
    }

    @Override
    public DataCollectionTaskType convertToEntityAttribute(String taskTypeCode) {
        if (taskTypeCode == null) {
            return null;
        }
        return DataCollectionTaskTypeRegistry.getByCode(taskTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown RequestType code: " + taskTypeCode));
    }
}