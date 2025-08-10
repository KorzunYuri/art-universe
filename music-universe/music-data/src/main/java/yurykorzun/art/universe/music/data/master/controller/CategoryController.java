package yurykorzun.art.universe.music.data.master.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import yurykorzun.art.universe.music.data.master.dto.CategoryHierarchyProjection;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.master.dto.CategorySaveRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BaseBatchLookupRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.BatchLookupResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityBindToExistingRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.EntityCreateAndBindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.service.CategoryService;
import yurykorzun.art.universe.music.data.master.service.BindingService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final BindingService bindingService;

    public CategoryController(CategoryService categoryService, BindingService bindingService) {
        this.categoryService = categoryService;
        this.bindingService = bindingService;
    }

    @GetMapping
    public Page<CategoryHierarchyProjection> findCategories(
        @RequestParam(required = false) String query,
        Pageable pageable
    ) {
        return categoryService.findCategories(query, pageable);
    }

    @GetMapping("/{id}")
    public CategoryHierarchyProjection getCategory(
        @PathVariable Long id
    ) {
        return categoryService.getCategory(id);
    }

    @GetMapping("/lookup")
    public List<LookupResultDTO> lookupCategories(
        @RequestParam String search,
        @RequestParam(required = false) Integer limit
    ) {
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search(search)
            .limit(limit)
            .build();
        return categoryService.lookupCategories(request);
    }
    
    @PostMapping("/lookup/batch")
    public BatchLookupResponseDTO batchLookupCategories(
        @Valid @RequestBody BaseBatchLookupRequestDTO request
    ) {
        return categoryService.batchLookupCategories(request);
    }

    @PostMapping
    public CategoryHierarchyProjection saveCategory(
        @Valid @RequestBody CategorySaveRequestDTO request
    ) {
        return categoryService.saveCategory(request);
    }

    @DeleteMapping("/{id}")
    public boolean deleteCategory(@PathVariable Long id) {
        boolean deleted = categoryService.deleteCategory(id);
        if (!deleted) {
            throw new CustomEntityNotFoundException("Category", id);
        }
        return true;
    }

    @GetMapping("/bound/{dataSource}")
    public List<BoundEntityProjection> findBoundCategories(
        @PathVariable DataSource dataSource,
        @RequestParam List<Long> externalIds
    ) {
        return categoryService.findBoundCategories(dataSource, externalIds);
    }

    @PostMapping("/bind/existing/{dataSource}/{externalId}")
    public BoundEntityProjection bindToExisting(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityBindToExistingRequestDTO request
    ) {
        return categoryService.bindToExisting(dataSource, externalId, request);
    }

    @PostMapping("/bind/new/{dataSource}/{externalId}")
    public BoundEntityProjection createAndBind(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId,
        @Valid @RequestBody EntityCreateAndBindRequestDTO request
    ) {
        return categoryService.createAndBind(dataSource, externalId, request);
    }

    @DeleteMapping("/unbind/{dataSource}/{externalId}")
    public boolean unbindCategory(
        @PathVariable DataSource dataSource,
        @PathVariable Long externalId
    ) {
        return categoryService.unbindCategory(dataSource, externalId);
    }
    
    @DeleteMapping("/unbind/{dataSource}/batch")
    public BatchUnbindResponseDTO batchUnbindCategories(
        @PathVariable DataSource dataSource,
        @Valid @RequestBody BatchUnbindRequestDTO request
    ) {
        return bindingService.batchUnbind(EntityType.CATEGORY, dataSource, request);
    }
}
