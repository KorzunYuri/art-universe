package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

/**
 * JPA converter for EntityType enum
 */
@Converter(autoApply = true)
public class EntityTypeConverter extends CodedConverter<MasterEntityType> {

    public EntityTypeConverter() {
        super(MasterEntityType.class);
    }
}
