package yurykorzun.art.universe.music.data.master.controller;

import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.service.RelationTypeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/relation-types")
public class RelationTypeController {

    private final RelationTypeService relationTypeService;

    public RelationTypeController(RelationTypeService relationTypeService) {
        this.relationTypeService = relationTypeService;
    }

    @GetMapping
    public List<RelationTypeDTO> findRelationTypes(
        @RequestParam(name = "search", required = false) String search
    ) {
        return relationTypeService.findRelationTypes(search);
    }

    @GetMapping("/{relationTypeId}")
    public RelationTypeWithApplicabilitiesDTO getRelationType(@PathVariable Long relationTypeId) {
        return relationTypeService.getRelationTypeWithApplicabilities(relationTypeId);
    }

    @GetMapping("/applicable/{sourceEntityType}/{targetEntityType}")
    public List<RelationTypeDTO> getApplicableTypes(
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable MasterEntityType targetEntityType
    ) {
        return relationTypeService.getApplicableTypes(sourceEntityType, targetEntityType);
    }

    @GetMapping("/{relationTypeId}/applicability")
    public List<RelationTypeApplicabilityDTO> getApplicabilities(@PathVariable Long relationTypeId) {
        return relationTypeService.getApplicabilities(relationTypeId);
    }
}
