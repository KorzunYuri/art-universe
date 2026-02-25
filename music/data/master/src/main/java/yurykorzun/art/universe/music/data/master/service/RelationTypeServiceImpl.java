package yurykorzun.art.universe.music.data.master.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RelationTypeServiceImpl implements RelationTypeService {

    private final RelationTypeRepository relationTypeRepository;
    private final RelationTypeApplicabilityRepository applicabilityRepository;

    public RelationTypeServiceImpl(
            RelationTypeRepository relationTypeRepository,
            RelationTypeApplicabilityRepository applicabilityRepository) {
        this.relationTypeRepository = relationTypeRepository;
        this.applicabilityRepository = applicabilityRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public RelationTypeWithApplicabilitiesDTO getRelationTypeWithApplicabilities(Long relationTypeId) {
        RelationType relationType = findByIdOrThrow(relationTypeId);
        List<RelationTypeApplicability> applicabilities = applicabilityRepository.findByRelationTypeId(relationTypeId);
        return RelationTypeWithApplicabilitiesDTO.builder()
            .id(relationType.getId())
            .name(relationType.getName())
            .reverseName(relationType.getReverseName())
            .isSymmetrical(relationType.isSymmetrical())
            .isSystem(relationType.isSystem())
            .applicabilities(applicabilities.stream().map(this::toApplicabilityDTO).collect(Collectors.toList()))
            .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationTypeDTO> findRelationTypes(String search) {
        List<RelationType> types;
        if (search != null && !search.isBlank()) {
            types = relationTypeRepository.findByNameContainingIgnoreCase(search);
        } else {
            types = relationTypeRepository.findAll();
        }
        return types.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationTypeDTO> getApplicableTypes(MasterEntityType sourceEntityType, MasterEntityType targetEntityType) {
        List<RelationTypeApplicability> applicabilities =
            applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(sourceEntityType, targetEntityType);
        return applicabilities.stream()
            .map(a -> toDTO(a.getRelationType()))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationTypeApplicabilityDTO> getApplicabilities(Long relationTypeId) {
        return applicabilityRepository.findByRelationTypeId(relationTypeId).stream()
            .map(this::toApplicabilityDTO)
            .collect(Collectors.toList());
    }

    private RelationType findByIdOrThrow(Long relationTypeId) {
        return relationTypeRepository.findById(relationTypeId)
            .orElseThrow(() -> new CustomEntityNotFoundException("relation_type", relationTypeId));
    }

    private RelationTypeDTO toDTO(RelationType entity) {
        return RelationTypeDTO.builder()
            .id(entity.getId())
            .name(entity.getName())
            .reverseName(entity.getReverseName())
            .isSymmetrical(entity.isSymmetrical())
            .isSystem(entity.isSystem())
            .build();
    }

    private RelationTypeApplicabilityDTO toApplicabilityDTO(RelationTypeApplicability entity) {
        return RelationTypeApplicabilityDTO.builder()
            .id(entity.getId())
            .relationTypeId(entity.getRelationTypeId())
            .sourceEntityType(entity.getSourceEntityType())
            .targetEntityType(entity.getTargetEntityType())
            .isDefault(entity.isDefault())
            .build();
    }
}
