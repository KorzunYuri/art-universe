package yurykorzun.art.universe.common.data.raw.task.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class TaskTypeConverter implements AttributeConverter<TaskType, String> {

    @Override
    public String convertToDatabaseColumn(TaskType taskType) {
        return taskType != null ? taskType.getCode() : null;
    }

    @Override
    public TaskType convertToEntityAttribute(String taskTypeCode) {
        if (taskTypeCode == null) {
            return null;
        }
        return TaskTypeRegistry.getByCode(taskTypeCode)
                .orElseThrow(() -> new IllegalArgumentException("Unknown TaskType code: " + taskTypeCode));
    }
}