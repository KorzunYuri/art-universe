package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefDto;
import yurykorzun.art.universe.music.data.master.dto.attribute.AttributeDefSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.attribute.AttributeTargetType;
import yurykorzun.art.universe.music.data.master.service.AttributeDefService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/attributes/definitions")
public class AttributeDefController {

    private final AttributeDefService attributeDefService;

    public AttributeDefController(AttributeDefService attributeDefService) {
        this.attributeDefService = attributeDefService;
    }

    @GetMapping
    public List<AttributeDefDto> findAll(
        @RequestParam(required = false) AttributeTargetType targetType
    ) {
        if (targetType != null) {
            return attributeDefService.findByTargetType(targetType);
        }
        return attributeDefService.findAll();
    }

    @GetMapping("/{id}")
    public AttributeDefDto getById(@PathVariable Long id) {
        return attributeDefService.getById(id);
    }

    @PostMapping
    public AttributeDefDto create(@Valid @RequestBody AttributeDefSaveRequestDTO request) {
        return attributeDefService.create(request);
    }

    @PutMapping("/{id}")
    public AttributeDefDto update(
        @PathVariable Long id,
        @Valid @RequestBody AttributeDefSaveRequestDTO request
    ) {
        return attributeDefService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return attributeDefService.delete(id);
    }
}
