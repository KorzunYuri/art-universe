package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.DimensionDto;
import yurykorzun.art.universe.music.data.master.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.DimensionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dimensions")
public class DimensionController {

    private final DimensionService dimensionService;

    public DimensionController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @GetMapping("/search")
    public Page<DimensionDto> searchDimensions(
        @RequestParam(required = false) String query,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            return dimensionService.searchDimensions(query, pageable);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to search dimensions: %s", e.getMessage()), e);
        }
    }

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupDimensions(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            return limit != null
                ? dimensionService.lookupDimensions(name, limit)
                : dimensionService.lookupDimensions(name);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to lookup dimensions: %s", e.getMessage()), e);
        }
    }

    @PostMapping
    public DimensionDto saveDimension(
        @Valid @RequestBody DimensionSaveRequestDTO request
    ) {
        try {
            return dimensionService.saveDimension(request);
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to save dimension: %s", e.getMessage()), e);
        }
    }

    @DeleteMapping("/{id}")
    public boolean deleteDimension(@PathVariable Long id) {
        try {
            boolean deleted = dimensionService.deleteDimension(id);
            if (!deleted) {
                throw new EntityNotFoundException("Dimension", id);
            }
            return true;
        } catch (EntityNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException(String.format("Failed to delete dimension: %s", e.getMessage()), e);
        }
    }
}
