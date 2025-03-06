package yurykorzun.art.universe.common.data.raw.task.entity;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class TaskStatusConverter implements AttributeConverter<TaskStatus, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TaskStatus status) {
        return status.getId();
    }

    @Override
    public TaskStatus convertToEntityAttribute(Integer id) {
        return TaskStatus.getById(id);
    }
}
