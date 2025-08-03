package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.DimensionDto;
import yurykorzun.art.universe.music.data.master.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.DimensionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dimensions")
public class DimensionController {

    private final DimensionService dimensionService;

    public DimensionController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @GetMapping
    public Page<DimensionDto> findDimensions(
        @RequestParam(required = false) String query,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        return dimensionService.findDimensions(query, pageable);
    }

    @GetMapping("/{id}")
    public DimensionDto getDimension(@PathVariable Long id) {
        return dimensionService.getDimension(id);
    }

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupDimensions(
        @RequestParam(required = false) String searchTerm,
        @RequestParam(required = false) Integer limit
    ) {
        return limit != null
            ? dimensionService.lookupDimensions(searchTerm, limit)
            : dimensionService.lookupDimensions(searchTerm);
    }

    @PostMapping
    public DimensionDto saveDimension(
        @Valid @RequestBody DimensionSaveRequestDTO request
    ) {
        return dimensionService.saveDimension(request);
    }

    @DeleteMapping("/{id}")
    public boolean deleteDimension(@PathVariable Long id) {
        boolean deleted = dimensionService.deleteDimension(id);
        if (!deleted) {
            throw new CustomEntityNotFoundException("Dimension", id);
        }
        return true;
    }
}
