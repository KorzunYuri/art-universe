package yurykorzun.art.universe.music.data.approved.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.DimensionDto;
import yurykorzun.art.universe.music.data.approved.dto.DimensionSaveRequestDTO;
import yurykorzun.art.universe.music.data.approved.dto.LookupResultDTO;
import yurykorzun.art.universe.music.data.approved.service.DimensionService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dimensions")
public class DimensionController {

    private final DimensionService dimensionService;

    public DimensionController(DimensionService dimensionService) {
        this.dimensionService = dimensionService;
    }

    @GetMapping("/search")
    public ResponseEntity<ResponseWrapper<Page<DimensionDto>>> searchDimensions(
        @RequestParam(required = false) String query,
        @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        try {
            Page<DimensionDto> dimensions = dimensionService.searchDimensions(query, pageable);
            return ResponseWrapper.success(dimensions);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to search dimensions: %s", e.getMessage()));
        }
    }

    @GetMapping("/lookup")
    public ResponseEntity<ResponseWrapper<List<LookupResultDTO>>> lookupDimensions(
        @RequestParam String name,
        @RequestParam(required = false) Integer limit
    ) {
        try {
            List<LookupResultDTO> dimensions = limit != null
                ? dimensionService.lookupDimensions(name, limit)
                : dimensionService.lookupDimensions(name);
            return ResponseWrapper.success(dimensions);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to lookup dimensions: %s", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ResponseWrapper<DimensionDto>> saveDimension(
        @Valid @RequestBody DimensionSaveRequestDTO request
    ) {
        try {
            DimensionDto savedDimension = dimensionService.saveDimension(request);
            return ResponseWrapper.success(savedDimension);
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to save dimension: %s", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseWrapper<Boolean>> deleteDimension(@PathVariable Long id) {
        try {
            boolean deleted = dimensionService.deleteDimension(id);
            if (deleted) {
                return ResponseWrapper.success(true);
            } else {
                return ResponseWrapper.failure("Dimension not found with id: " + id);
            }
        } catch (Exception e) {
            return ResponseWrapper.failure(String.format("Failed to delete dimension: %s", e.getMessage()));
        }
    }
}
