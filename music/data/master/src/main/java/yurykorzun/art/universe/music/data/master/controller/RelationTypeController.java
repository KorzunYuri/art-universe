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

    @GetMapping("/{id}")
    public RelationTypeWithApplicabilitiesDTO getRelationType(@PathVariable Long id) {
        return relationTypeService.getRelationTypeWithApplicabilities(id);
    }

    @PostMapping
    public RelationTypeDTO createRelationType(@RequestBody RelationTypeDTO request) {
        return relationTypeService.createRelationType(
            request.getName(), request.getReverseName(), request.isSymmetrical());
    }

    @PutMapping("/{id}")
    public RelationTypeDTO updateRelationType(
        @PathVariable Long id,
        @RequestBody RelationTypeDTO request
    ) {
        return relationTypeService.updateRelationType(
            id, request.getName(), request.getReverseName(), request.isSymmetrical());
    }

    @DeleteMapping("/{id}")
    public void deleteRelationType(@PathVariable Long id) {
        relationTypeService.deleteRelationType(id);
    }

    @GetMapping("/applicable/{sourceEntityType}/{targetEntityType}")
    public List<RelationTypeDTO> getApplicableTypes(
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable MasterEntityType targetEntityType
    ) {
        return relationTypeService.getApplicableTypes(sourceEntityType, targetEntityType);
    }

    @PostMapping("/{id}/applicability")
    public RelationTypeApplicabilityDTO addApplicability(
        @PathVariable Long id,
        @RequestBody RelationTypeApplicabilityDTO request
    ) {
        return relationTypeService.addApplicability(
            id, request.getSourceEntityType(), request.getTargetEntityType(), request.isDefault());
    }

    @DeleteMapping("/{id}/applicability/{sourceEntityType}/{targetEntityType}")
    public void removeApplicability(
        @PathVariable Long id,
        @PathVariable MasterEntityType sourceEntityType,
        @PathVariable MasterEntityType targetEntityType
    ) {
        relationTypeService.removeApplicability(id, sourceEntityType, targetEntityType);
    }

    @GetMapping("/{id}/applicability")
    public List<RelationTypeApplicabilityDTO> getApplicabilities(@PathVariable Long id) {
        return relationTypeService.getApplicabilities(id);
    }
}
