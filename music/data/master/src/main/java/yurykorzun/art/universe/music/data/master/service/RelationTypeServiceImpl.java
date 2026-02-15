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
    @Transactional
    public RelationTypeDTO createRelationType(String name, String reverseName, boolean symmetrical) {
        RelationType relationType = RelationType.builder()
            .name(name)
            .reverseName(reverseName)
            .symmetrical(symmetrical)
            .build();
        relationType = relationTypeRepository.save(relationType);
        return toDTO(relationType);
    }

    @Override
    @Transactional
    public RelationTypeDTO updateRelationType(Long id, String name, String reverseName, boolean symmetrical) {
        RelationType relationType = findByIdOrThrow(id);
        rejectIfSystem(relationType, "modify");
        relationType.setName(name);
        relationType.setReverseName(reverseName);
        relationType.setSymmetrical(symmetrical);
        relationType = relationTypeRepository.save(relationType);
        return toDTO(relationType);
    }

    @Override
    @Transactional
    public void deleteRelationType(Long id) {
        RelationType relationType = findByIdOrThrow(id);
        rejectIfSystem(relationType, "delete");
        relationTypeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public RelationTypeDTO getRelationType(Long id) {
        return toDTO(findByIdOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public RelationTypeWithApplicabilitiesDTO getRelationTypeWithApplicabilities(Long id) {
        RelationType relationType = findByIdOrThrow(id);
        List<RelationTypeApplicability> applicabilities = applicabilityRepository.findByRelationTypeId(id);
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
    @Transactional
    public RelationTypeApplicabilityDTO addApplicability(Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType, boolean isDefault) {
        RelationType relationType = findByIdOrThrow(relationTypeId);
        rejectIfSystem(relationType, "modify applicability of");

        if (applicabilityRepository.existsByRelationTypeIdAndSourceEntityTypeAndTargetEntityType(
                relationTypeId, sourceEntityType, targetEntityType)) {
            throw new IllegalArgumentException(
                String.format("Applicability already exists for relation type %d and entity types %s -> %s",
                    relationTypeId, sourceEntityType.getName(), targetEntityType.getName()));
        }

        RelationTypeApplicability applicability = RelationTypeApplicability.builder()
            .relationTypeId(relationTypeId)
            .sourceEntityType(sourceEntityType)
            .targetEntityType(targetEntityType)
            .isDefault(isDefault)
            .build();
        applicability = applicabilityRepository.save(applicability);
        return toApplicabilityDTO(applicability);
    }

    @Override
    @Transactional
    public void removeApplicability(Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType) {
        RelationType relationType = findByIdOrThrow(relationTypeId);
        rejectIfSystem(relationType, "modify applicability of");
        applicabilityRepository.deleteByRelationTypeIdAndSourceEntityTypeAndTargetEntityType(
            relationTypeId, sourceEntityType, targetEntityType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelationTypeApplicabilityDTO> getApplicabilities(Long relationTypeId) {
        return applicabilityRepository.findByRelationTypeId(relationTypeId).stream()
            .map(this::toApplicabilityDTO)
            .collect(Collectors.toList());
    }

    private RelationType findByIdOrThrow(Long id) {
        return relationTypeRepository.findById(id)
            .orElseThrow(() -> new CustomEntityNotFoundException("relation_type", id));
    }

    private void rejectIfSystem(RelationType relationType, String action) {
        if (relationType.isSystem()) {
            throw new IllegalArgumentException(
                String.format("Cannot %s system relation type '%s'", action, relationType.getName()));
        }
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
