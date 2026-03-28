package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.EntityAttributeValueSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.service.EntityAttributeValueService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes")
public class EntityAttributeValueController {

    private final EntityAttributeValueService valueService;

    public EntityAttributeValueController(EntityAttributeValueService valueService) {
        this.valueService = valueService;
    }

    @GetMapping("/{targetType}/{targetId}")
    public List<EntityAttributeValueDto> findByTarget(
        @PathVariable AttributeTargetType targetType,
        @PathVariable Long targetId
    ) {
        return valueService.findByTarget(targetType, targetId);
    }

    @PostMapping("/{targetType}/{targetId}")
    public EntityAttributeValueDto create(
        @PathVariable AttributeTargetType targetType,
        @PathVariable Long targetId,
        @Valid @RequestBody EntityAttributeValueSaveRequestDTO request
    ) {
        return valueService.create(targetType, targetId, request);
    }

    @DeleteMapping("/values/{valueId}")
    public boolean delete(@PathVariable Long valueId) {
        return valueService.delete(valueId);
    }
}
