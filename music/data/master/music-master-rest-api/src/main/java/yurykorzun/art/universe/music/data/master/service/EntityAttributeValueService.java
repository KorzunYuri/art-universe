package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;

import java.util.List;

public interface EntityAttributeValueService {

    List<EntityAttributeValueDto> findByTarget(AttributeTargetType targetType, Long targetId);

    EntityAttributeValueDto create(AttributeTargetType targetType, Long targetId, EntityAttributeValueSaveRequestDTO request);

    boolean delete(Long valueId);
}
