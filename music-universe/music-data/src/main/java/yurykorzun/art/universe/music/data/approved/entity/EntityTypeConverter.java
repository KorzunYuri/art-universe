package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.CodedConverter;

/**
 * JPA converter for EntityType enum
 */
@Converter(autoApply = true)
public class EntityTypeConverter extends CodedConverter<EntityType> {

    public EntityTypeConverter() {
        super(EntityType.class);
    }
}
