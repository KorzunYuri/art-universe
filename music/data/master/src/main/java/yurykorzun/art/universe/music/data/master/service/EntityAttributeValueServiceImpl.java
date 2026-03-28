package yurykorzun.art.universe.music.data.master.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.attribute.*;
import yurykorzun.art.universe.music.data.master.repository.AttributeDefRepository;
import yurykorzun.art.universe.music.data.master.repository.EntityAttributeValueRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EntityAttributeValueServiceImpl implements EntityAttributeValueService {

    private final EntityAttributeValueRepository valueRepository;
    private final AttributeDefRepository attributeDefRepository;

    public EntityAttributeValueServiceImpl(
        EntityAttributeValueRepository valueRepository,
        AttributeDefRepository attributeDefRepository
    ) {
        this.valueRepository = valueRepository;
        this.attributeDefRepository = attributeDefRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<EntityAttributeValueDto> findByTarget(AttributeTargetType targetType, Long targetId) {
        return valueRepository.findByTarget(targetType, targetId).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EntityAttributeValueDto create(
        AttributeTargetType targetType,
        Long targetId,
        EntityAttributeValueSaveRequestDTO request
    ) {
        AttributeDef def = attributeDefRepository.findById(request.getAttributeDefId())
            .orElseThrow(() -> new CustomEntityNotFoundException("AttributeDef", request.getAttributeDefId()));

        EntityAttributeValue value = EntityAttributeValue.builder()
            .targetType(targetType)
            .targetId(targetId)
            .attributeDef(def)
            .numericValue(request.getNumericValue())
            .stringValue(request.getStringValue())
            .dateValue(request.getDateValue())
            .boolValue(request.getBoolValue())
            .eventDate(request.getEventDate())
            .validFrom(request.getValidFrom())
            .validTill(request.getValidTill())
            .sourceType(request.getSourceType() != null ? request.getSourceType() : AttributeSourceType.MANUAL)
            .sourceRef(request.getSourceRef())
            .confidence(request.getConfidence())
            .origin((short) 0)
            .build();

        value = valueRepository.save(value);
        return mapToDto(value);
    }

    @Override
    @Transactional
    public boolean delete(Long valueId) {
        return valueRepository.findById(valueId)
            .map(value -> {
                valueRepository.delete(value);
                return true;
            })
            .orElse(false);
    }

    private EntityAttributeValueDto mapToDto(EntityAttributeValue value) {
        AttributeDef def = value.getAttributeDef();
        return EntityAttributeValueDto.builder()
            .id(value.getId())
            .targetType(value.getTargetType())
            .targetId(value.getTargetId())
            .attributeDefId(def.getId())
            .attributeCode(def.getCode())
            .attributeName(def.getName())
            .attributeDataType(def.getDataType())
            .attributeTemporalType(def.getTemporalType())
            .numericValue(value.getNumericValue())
            .stringValue(value.getStringValue())
            .dateValue(value.getDateValue())
            .boolValue(value.getBoolValue())
            .eventDate(value.getEventDate())
            .validFrom(value.getValidFrom())
            .validTill(value.getValidTill())
            .sourceType(value.getSourceType())
            .sourceRef(value.getSourceRef())
            .confidence(value.getConfidence())
            .createdAt(value.getCreatedAt())
            .updatedAt(value.getUpdatedAt())
            .build();
    }
}
