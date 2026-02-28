package yurykorzun.art.universe.common.persistence.converter;

import jakarta.persistence.Converter;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

/**
 * JPA converter for MasterEntityType enum
 */
@Converter(autoApply = true)
public class EntityTypeConverter extends CodedConverter<MasterEntityType> {

    public EntityTypeConverter() {
        super(MasterEntityType.class);
    }
}
