package yurykorzun.art.universe.music.data.master.service;

import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import java.util.List;

public interface RelationTypeService {

    RelationTypeWithApplicabilitiesDTO getRelationTypeWithApplicabilities(Long relationTypeId);

    List<RelationTypeDTO> findRelationTypes(String search);

    List<RelationTypeDTO> getApplicableTypes(MasterEntityType sourceEntityType, MasterEntityType targetEntityType);

    List<RelationTypeApplicabilityDTO> getApplicabilities(Long relationTypeId);
}
