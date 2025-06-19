package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

@Converter(autoApply = true)
public class DataSourceConverter extends CodedConverter<DataSource>{

    public DataSourceConverter() {
        super(DataSource.class);
    }
}
