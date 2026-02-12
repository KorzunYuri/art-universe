package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.List;

public interface RelationTypeService {

    RelationTypeDTO createRelationType(String name, String reverseName, boolean symmetrical);

    RelationTypeDTO updateRelationType(Long id, String name, String reverseName, boolean symmetrical);

    void deleteRelationType(Long id);

    RelationTypeDTO getRelationType(Long id);

    RelationTypeWithApplicabilitiesDTO getRelationTypeWithApplicabilities(Long id);

    List<RelationTypeDTO> findRelationTypes(String search);

    List<RelationTypeDTO> getApplicableTypes(MasterEntityType sourceEntityType, MasterEntityType targetEntityType);

    RelationTypeApplicabilityDTO addApplicability(Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType, boolean isDefault);

    void removeApplicability(Long relationTypeId, MasterEntityType sourceEntityType, MasterEntityType targetEntityType);

    List<RelationTypeApplicabilityDTO> getApplicabilities(Long relationTypeId);
}
