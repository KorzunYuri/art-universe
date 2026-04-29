package yurykorzun.art.universe.music.data.master.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeDef;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeDefApplicability;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.repository.AttributeDefRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AttributeDefServiceImpl implements AttributeDefService {

    private final AttributeDefRepository attributeDefRepository;

    public AttributeDefServiceImpl(AttributeDefRepository attributeDefRepository) {
        this.attributeDefRepository = attributeDefRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefDto> findAll() {
        return attributeDefRepository.findAllWithApplicabilities().stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AttributeDefDto> findByTargetType(AttributeTargetType targetType) {
        return attributeDefRepository.findByTargetType(targetType).stream()
            .map(this::mapToDto)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public AttributeDefDto getById(Long id) {
        AttributeDef def = attributeDefRepository.findById(id)
            .orElseThrow(() -> new CustomEntityNotFoundException("AttributeDef", id));
        return mapToDto(def);
    }

    @Override
    @Transactional
    public AttributeDefDto create(AttributeDefSaveRequestDTO request) {
        AttributeDef def = AttributeDef.builder()
            .code(request.getCode())
            .name(request.getName())
            .description(request.getDescription())
            .dataType(request.getDataType())
            .temporalType(request.getTemporalType())
            .computationType(request.getComputationType())
            .dataSource(request.getDataSource())
            .formula(request.getFormula())
            .multiValue(request.isMultiValue())
            .system(false)
            .applicabilities(new ArrayList<>())
            .build();

        for (AttributeTargetType targetType : request.getApplicableTo()) {
            AttributeDefApplicability applicability = AttributeDefApplicability.builder()
                .attributeDef(def)
                .targetType(targetType)
                .build();
            def.getApplicabilities().add(applicability);
        }

        def = attributeDefRepository.save(def);
        return mapToDto(def);
    }

    @Override
    @Transactional
    public AttributeDefDto update(Long id, AttributeDefSaveRequestDTO request) {
        AttributeDef def = attributeDefRepository.findById(id)
            .orElseThrow(() -> new CustomEntityNotFoundException("AttributeDef", id));

        if (def.isSystem()) {
            throw new IllegalStateException("Cannot modify system attribute definition: " + def.getCode());
        }

        def.setCode(request.getCode());
        def.setName(request.getName());
        def.setDescription(request.getDescription());
        def.setDataType(request.getDataType());
        def.setTemporalType(request.getTemporalType());
        def.setComputationType(request.getComputationType());
        def.setDataSource(request.getDataSource());
        def.setFormula(request.getFormula());
        def.setMultiValue(request.isMultiValue());

        def.getApplicabilities().clear();
        for (AttributeTargetType targetType : request.getApplicableTo()) {
            AttributeDefApplicability applicability = AttributeDefApplicability.builder()
                .attributeDef(def)
                .targetType(targetType)
                .build();
            def.getApplicabilities().add(applicability);
        }

        def = attributeDefRepository.save(def);
        return mapToDto(def);
    }

    @Override
    @Transactional
    public boolean delete(Long id) {
        return attributeDefRepository.findById(id)
            .map(def -> {
                if (def.isSystem()) {
                    throw new IllegalStateException("Cannot delete system attribute definition: " + def.getCode());
                }
                attributeDefRepository.delete(def);
                return true;
            })
            .orElse(false);
    }

    private AttributeDefDto mapToDto(AttributeDef def) {
        List<AttributeTargetType> applicableTo = def.getApplicabilities() != null
            ? def.getApplicabilities().stream()
                .map(AttributeDefApplicability::getTargetType)
                .collect(Collectors.toList())
            : List.of();

        return AttributeDefDto.builder()
            .id(def.getId())
            .code(def.getCode())
            .name(def.getName())
            .description(def.getDescription())
            .dataType(def.getDataType())
            .temporalType(def.getTemporalType())
            .computationType(def.getComputationType())
            .dataSource(def.getDataSource())
            .formula(def.getFormula())
            .multiValue(def.isMultiValue())
            .system(def.isSystem())
            .applicableTo(applicableTo)
            .build();
    }
}
