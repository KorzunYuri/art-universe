package yurykorzun.art.universe.common.data.raw.task.entity;

import yurykorzun.art.universe.common.CodedConverter;

import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TaskTypeConverter extends CodedConverter<TaskType> {

    public TaskTypeConverter() {
        super(TaskType.class);
    }

}