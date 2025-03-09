package yurykorzun.art.universe.common.data.raw.task.entity;

import yurykorzun.art.universe.common.CodedConverter;

import jakarta.persistence.Converter;

@Converter
public class TaskStatusConverter extends CodedConverter<TaskStatus> {

    public TaskStatusConverter() {
        super(TaskStatus.class);
    }
}
